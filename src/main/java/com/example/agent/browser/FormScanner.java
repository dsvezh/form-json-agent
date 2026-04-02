package com.example.agent.browser;

import com.example.agent.model.FieldDependencySnapshot;
import com.example.agent.model.FieldDescriptor;
import com.example.agent.model.FieldOption;
import com.example.agent.model.FormSchema;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Читает форму со страницы и строит FormSchema.
 *
 * Что важно в этой версии:
 * - селекторы по id строятся безопасно даже для id с точками;
 * - disabled-поля не участвуют в анализе зависимостей;
 * - placeholder-значения не выбираются;
 * - таймауты на selectOption уменьшены, чтобы приложение не "висело" минутами;
 * - анализ зависимостей ограничен, чтобы не перебирать без необходимости все select подряд.
 */
public class FormScanner {
    private static final double WAIT_TIMEOUT_MS = 10_000;
    private static final double SHORT_UI_WAIT_MS = 200;
    private static final double SELECT_TIMEOUT_MS = 2_000;

    /**
     * Ограничиваем число пар полей для dependency scan.
     * Это защищает от долгого перебора на больших формах.
     */
    private static final int MAX_DEPENDENCY_PAIRS = 5;

    public FormSchema scan(Page page, String formSelector, boolean scanCustomDropdowns) {
        System.out.println("FormScanner: waiting for selector: " + formSelector);

        Locator form = page.locator(formSelector).first();
        form.waitFor(new Locator.WaitForOptions().setTimeout(WAIT_TIMEOUT_MS));

        System.out.println("FormScanner: selector found");

        FormSchema schema = new FormSchema();
        schema.setUrl(page.url());

        List<FieldDescriptor> fields = new ArrayList<>();

        System.out.println("FormScanner: scanning native fields");
        fields.addAll(scanNativeFields(page, form));
        System.out.println("FormScanner: native fields found = " + fields.size());

        if (scanCustomDropdowns) {
            System.out.println("FormScanner: scanning custom dropdowns");
            List<FieldDescriptor> customFields = scanCustomDropdowns(page, form);
            System.out.println("FormScanner: custom dropdown fields found = " + customFields.size());
            fields.addAll(customFields);
        }

        List<FieldDescriptor> mergedFields = mergeByKey(fields);
        schema.setFields(mergedFields);

        System.out.println("FormScanner: merged fields count = " + mergedFields.size());

        System.out.println("FormScanner: capturing dependencies");
        List<FieldDependencySnapshot> snapshots = captureDependencies(page, schema);
        schema.setDependencySnapshots(snapshots);
        System.out.println("FormScanner: dependency snapshots count = " + snapshots.size());

        return schema;
    }

    private List<FieldDescriptor> scanNativeFields(Page page, Locator form) {
        List<FieldDescriptor> fields = new ArrayList<>();
        List<ElementHandle> elements = form.locator("input, select, textarea").elementHandles();

        System.out.println("FormScanner: native DOM elements count = " + elements.size());

        for (ElementHandle element : elements) {
            String tag = stringValue(element.evaluate("el => el.tagName.toLowerCase()"));
            String name = attr(element, "name");
            String id = attr(element, "id");
            String key = firstNonBlank(name, id, "unnamed_field");
            String type = "input".equals(tag) ? firstNonBlank(attr(element, "type"), "text") : tag;
            boolean required = element.getAttribute("required") != null;
            String label = resolveLabel(page, element, id, name);

            FieldDescriptor descriptor = new FieldDescriptor(
                key,
                label,
                normalizeType(tag, type),
                required
            );

            descriptor.setSelector(buildSelector(id, name, null));

            if ("select".equals(tag)) {
                descriptor.setOptions(readNativeSelectOptions(element));
            }

            fields.add(descriptor);

            System.out.println("FormScanner: native field found -> key=" + descriptor.getKey()
                               + ", type=" + descriptor.getType()
                               + ", selector=" + descriptor.getSelector()
                               + ", options=" + descriptor.getOptions().size());
        }

        return fields;
    }

    private List<FieldDescriptor> scanCustomDropdowns(Page page, Locator form) {
        List<FieldDescriptor> fields = new ArrayList<>();
        Locator dropdowns = form.locator("[role='combobox'], [aria-haspopup='listbox']");
        int dropdownCount = dropdowns.count();

        System.out.println("FormScanner: custom dropdown candidates = " + dropdownCount);

        for (int i = 0; i < dropdownCount; i++) {
            Locator dropdown = dropdowns.nth(i);

            String id = safeAttribute(dropdown, "id");
            String name = safeAttribute(dropdown, "name");
            String label = resolveCustomLabel(page, dropdown, id, name, i);
            String key = firstNonBlank(name, id, toMachineKey(label), "custom_dropdown_" + i);

            FieldDescriptor descriptor = new FieldDescriptor(key, label, "select", isRequired(dropdown));
            descriptor.setCustomDropdown(true);
            descriptor.setSelector(buildSelector(id, name, i));
            descriptor.setOptions(readCustomDropdownOptions(page, dropdown));

            fields.add(descriptor);

            System.out.println("FormScanner: custom dropdown found -> key=" + descriptor.getKey()
                               + ", selector=" + descriptor.getSelector()
                               + ", options=" + descriptor.getOptions().size());
        }

        return fields;
    }

    private List<FieldDependencySnapshot> captureDependencies(Page page, FormSchema schema) {
        List<FieldDependencySnapshot> snapshots = new ArrayList<>();

        List<FieldDescriptor> selectableFields = schema.getFields().stream()
                                                       .filter(field -> "select".equals(field.getType()))
                                                       .filter(field -> !field.getOptions().isEmpty())
                                                       .toList();

        System.out.println("FormScanner: selectable fields for dependency analysis = " + selectableFields.size());

        int checkedPairs = 0;

        for (int i = 0; i < selectableFields.size() - 1; i++) {
            if (checkedPairs >= MAX_DEPENDENCY_PAIRS) {
                System.out.println("FormScanner: dependency analysis limit reached");
                break;
            }

            FieldDescriptor parent = selectableFields.get(i);
            FieldDescriptor child = selectableFields.get(i + 1);

            if (parent.getSelector() == null || child.getSelector() == null) {
                System.out.println("FormScanner: skipping dependency pair due to missing selector");
                continue;
            }

            Locator parentLocator = page.locator(parent.getSelector()).first();
            Locator childLocator = page.locator(child.getSelector()).first();

            if (parentLocator.count() == 0 || childLocator.count() == 0) {
                System.out.println("FormScanner: skipping dependency pair because locator not found");
                continue;
            }

            if (!isInteractable(parentLocator)) {
                System.out.println("FormScanner: skipping disabled or invisible parent field " + parent.getKey());
                continue;
            }

            if (!isVisibleEnough(childLocator)) {
                System.out.println("FormScanner: skipping child field because it is not visible " + child.getKey());
                continue;
            }

            System.out.println("FormScanner: checking dependency parent=" + parent.getKey() + " child=" + child.getKey());
            checkedPairs++;

            for (FieldOption option : parent.getOptions()) {
                if (isPlaceholderOption(option)) {
                    System.out.println("FormScanner: skipping placeholder option " + option.value() + " for " + parent.getKey());
                    continue;
                }

                boolean selected = selectValue(page, parent, option.value());
                if (!selected) {
                    System.out.println("FormScanner: failed to select option " + option.value() + " for " + parent.getKey());
                    continue;
                }

                page.waitForTimeout(SHORT_UI_WAIT_MS);
                List<FieldOption> childOptions = rereadOptions(page, child);

                List<FieldOption> filteredChildOptions = childOptions.stream()
                                                                     .filter(optionItem -> !isPlaceholderOption(optionItem))
                                                                     .distinct()
                                                                     .toList();

                if (!filteredChildOptions.isEmpty()) {
                    child.setDependsOn(parent.getKey());

                    FieldDependencySnapshot snapshot = new FieldDependencySnapshot();
                    snapshot.setParentField(parent.getKey());
                    snapshot.setParentValue(option.value());
                    snapshot.setChildField(child.getKey());
                    snapshot.setOptions(filteredChildOptions);
                    snapshots.add(snapshot);

                    System.out.println("FormScanner: dependency snapshot added -> "
                                       + parent.getKey() + "=" + option.value()
                                       + " -> " + child.getKey()
                                       + " options=" + filteredChildOptions.size());
                }
            }
        }

        return snapshots;
    }

    private List<FieldOption> rereadOptions(Page page, FieldDescriptor field) {
        Locator locator = page.locator(field.getSelector()).first();

        if (locator.count() == 0) {
            return List.of();
        }

        if (field.isCustomDropdown()) {
            return readCustomDropdownOptions(page, locator);
        }

        ElementHandle handle = locator.elementHandle();
        if (handle == null) {
            return List.of();
        }

        return readNativeSelectOptions(handle);
    }

    private boolean selectValue(Page page, FieldDescriptor field, String value) {
        try {
            Locator locator = page.locator(field.getSelector()).first();

            if (locator.count() == 0) {
                return false;
            }

            if (!isInteractable(locator)) {
                return false;
            }

            if (field.isCustomDropdown()) {
                locator.click(new Locator.ClickOptions().setTimeout(SELECT_TIMEOUT_MS));
                Locator options = page.locator("[role='option']");

                for (int i = 0; i < options.count(); i++) {
                    Locator option = options.nth(i);
                    String optionText = option.innerText().trim();
                    String optionValue = firstNonBlank(safeAttribute(option, "data-value"), optionText);

                    if (Objects.equals(optionValue, value) || Objects.equals(optionText, value)) {
                        option.click(new Locator.ClickOptions().setTimeout(SELECT_TIMEOUT_MS));
                        return true;
                    }
                }

                page.keyboard().press("Escape");
                return false;
            }

            locator.selectOption(value, new Locator.SelectOptionOptions().setTimeout(SELECT_TIMEOUT_MS));
            return true;
        } catch (Exception e) {
            System.out.println("FormScanner: selectValue failed for field=" + field.getKey()
                               + ", value=" + value
                               + ", reason=" + e.getMessage());
            return false;
        }
    }

    private List<FieldOption> readNativeSelectOptions(ElementHandle selectElement) {
        List<FieldOption> options = new ArrayList<>();

        for (ElementHandle option : selectElement.querySelectorAll("option")) {
            String value = firstNonBlank(option.getAttribute("value"), option.textContent());
            String optionLabel = firstNonBlank(option.textContent(), value);

            if (!optionLabel.isBlank()) {
                FieldOption fieldOption = new FieldOption(value.trim(), optionLabel.trim());
                if (!isPlaceholderOption(fieldOption)) {
                    options.add(fieldOption);
                }
            }
        }

        return options;
    }

    private List<FieldOption> readCustomDropdownOptions(Page page, Locator dropdown) {
        List<FieldOption> options = new ArrayList<>();

        try {
            if (!isInteractable(dropdown)) {
                return List.of();
            }

            dropdown.click(new Locator.ClickOptions().setTimeout(SELECT_TIMEOUT_MS));
            page.waitForTimeout(SHORT_UI_WAIT_MS);

            Locator optionLocator = page.locator("[role='option'], [role='listbox'] [data-value], [role='listbox'] li");
            int count = optionLocator.count();

            for (int i = 0; i < count; i++) {
                Locator option = optionLocator.nth(i);
                String label = safeInnerText(option);
                String value = firstNonBlank(safeAttribute(option, "data-value"), label);

                if (!label.isBlank()) {
                    FieldOption fieldOption = new FieldOption(value.trim(), label.trim());
                    if (!isPlaceholderOption(fieldOption)) {
                        options.add(fieldOption);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("FormScanner: failed to read custom dropdown options, reason=" + e.getMessage());
        } finally {
            try {
                page.keyboard().press("Escape");
            } catch (Exception ignored) {
            }
        }

        return uniqueOptions(options);
    }

    private String resolveLabel(Page page, ElementHandle element, String id, String name) {
        if (id != null && !id.isBlank()) {
            Locator label = page.locator("label[for='" + id + "']");
            if (label.count() > 0) {
                return label.first().innerText().trim();
            }
        }

        String ariaLabel = attr(element, "aria-label");
        String placeholder = attr(element, "placeholder");
        return firstNonBlank(ariaLabel, placeholder, name, "unknown");
    }

    private String resolveCustomLabel(Page page, Locator dropdown, String id, String name, int index) {
        if (id != null && !id.isBlank()) {
            Locator label = page.locator("label[for='" + id + "']");
            if (label.count() > 0) {
                return label.first().innerText().trim();
            }
        }

        String ariaLabel = safeAttribute(dropdown, "aria-label");
        String labelledBy = safeAttribute(dropdown, "aria-labelledby");
        if (!labelledBy.isBlank()) {
            Locator labelled = page.locator("#" + labelledBy);
            if (labelled.count() > 0) {
                return labelled.first().innerText().trim();
            }
        }

        return firstNonBlank(ariaLabel, name, "custom_dropdown_" + index);
    }

    private String buildSelector(String id, String name, Integer fallbackIndex) {
        if (id != null && !id.isBlank()) {
            // Такой селектор безопасен даже если в id есть точки, двоеточия и другие спецсимволы CSS.
            return "[id='" + id + "']";
        }

        if (name != null && !name.isBlank()) {
            return "[name='" + name + "']";
        }

        if (fallbackIndex != null) {
            return "[role='combobox']:nth-of-type(" + (fallbackIndex + 1) + ")";
        }

        return null;
    }

    private List<FieldDescriptor> mergeByKey(List<FieldDescriptor> fields) {
        List<FieldDescriptor> merged = new ArrayList<>();

        for (FieldDescriptor field : fields) {
            Optional<FieldDescriptor> existing = merged.stream()
                                                       .filter(candidate -> candidate.getKey().equals(field.getKey()))
                                                       .findFirst();

            if (existing.isPresent()) {
                if (existing.get().getOptions().isEmpty() && !field.getOptions().isEmpty()) {
                    existing.get().setOptions(field.getOptions());
                }
                if (existing.get().getSelector() == null && field.getSelector() != null) {
                    existing.get().setSelector(field.getSelector());
                }
                if (!existing.get().isCustomDropdown() && field.isCustomDropdown()) {
                    existing.get().setCustomDropdown(true);
                }
            } else {
                merged.add(field);
            }
        }

        return merged;
    }

    private List<FieldOption> uniqueOptions(List<FieldOption> options) {
        return options.stream()
                      .filter(option -> option.label() != null && !option.label().isBlank())
                      .distinct()
                      .collect(Collectors.toList());
    }

    private boolean isRequired(Locator locator) {
        return !safeAttribute(locator, "required").isBlank()
               || "true".equalsIgnoreCase(safeAttribute(locator, "aria-required"));
    }

    private boolean isInteractable(Locator locator) {
        try {
            return locator.isVisible() && locator.isEnabled();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isVisibleEnough(Locator locator) {
        try {
            return locator.isVisible();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isPlaceholderOption(FieldOption option) {
        String value = option.value() == null ? "" : option.value().trim();
        String label = option.label() == null ? "" : option.label().trim().toLowerCase();

        return value.isBlank()
               || "null".equalsIgnoreCase(value)
               || label.contains("выберите")
               || label.contains("select");
    }

    private String attr(ElementHandle element, String name) {
        String value = element.getAttribute(name);
        return value == null ? "" : value.trim();
    }

    private String safeAttribute(Locator locator, String name) {
        try {
            String value = locator.getAttribute(name);
            return value == null ? "" : value.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String safeInnerText(Locator locator) {
        try {
            return locator.innerText().trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String normalizeType(String tag, String type) {
        if ("select".equals(tag)) {
            return "select";
        }
        if ("textarea".equals(tag)) {
            return "textarea";
        }
        return type == null || type.isBlank() ? "text" : type.toLowerCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String toMachineKey(String label) {
        return label.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_+|_+$", "");
    }
}

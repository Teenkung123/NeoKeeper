# Station hex-name fix

- Enhanced `ItemTextFormatter` with `displayNameFormatted` which converts hex legacy codes (e.g. `&x&f&e...`) into MiniMessage-friendly `<#hex>` tokens, fixing the garbled lore when items use RGB names.
- Updated all station GUIs to call the new helper so recipe titles/material entries retain the correct colors without the verbose `&x&` spam.
- Pending Gradle build (still no wrapper locally). Please rebuild on your environment.


2025-02-27 Гога и mvk согласовали решение:
1. Создается новый проект: ts4-uskat/org.toxsoft.uskat.layout.
2. Классы ISkNetNode, ISkServer, ISkClusterNode: переезжают в новый проект uskat.layout.
3.  В uskat.layout, помимо выше указанных классов, будет размещен библиотечный код создания, управления этих классов и их объектов. 
4. Также в  uskat.layout будет размещена служба ISkLayoutService которая будет управлять этими объектами.
5. Для SkIDE будет создан отдельный плагин uskat.layout.skide использующий службу ISkLayoutService.
6. Сервер использует (зависит) от uskat.layout на уровне библиотечного кода.
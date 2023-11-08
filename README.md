# patched-minecraft-authlib
Библиотека authlib с вырезанной проверкой домена и цифровой подписи.
Подгружать скины можно с любого домена.
_jars - содержит скомпилированные пропатчиные версии authlib._

### Ссылки

Ссылки храняться в файле _YggdrasilMinecraftSessionService.java_ в виде констант.

```java
Путь до файла YggdrasilMinecraftSessionService.java

com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService.java
```
```Java
private final String profileUrl = "...";
private final String checkUrl = "...";
private final String joinUrl = "...";
```

Их заменяем на свои скрипты.
Так же ссылки можно заменить через InClassTranslator или InJarTranslator непосредственно в самом jar файле.


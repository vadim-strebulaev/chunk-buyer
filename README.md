# chunk-buyer

Минимальный Paper/Spigot-плагин для покупки чанков за отдельную (донатную) валюту.

## Что делает
- `/chunkbuyer buy` — покупает текущий чанк за `chunk-price`.
- `/chunkbuyer unclaim` — снимает приват со своего чанка.
- `/chunkbuyer info` — показывает владельца чанка.
- `/chunkbuyer balance` — показывает баланс донатной валюты.
- `/chunkbuyer give <player> <amount>` — выдача валюты (только `chunkbuyer.admin`).
- `/chunkbuyer addmember <player>` — добавить игрока в текущий приват как участника.
- `/chunkbuyer explosions <on|off>` — для OP включает/выключает разрушение блоков от взрывов в привате.
- `/chunkbuyer borders` — подсвечивает границы текущего региона (вертикальные углы и шаг 16 по горизонтали).

Защита чанков:
- Ломать/ставить блоки в чужом привате нельзя.
- Взрывы удаляют блоки везде, кроме чужих приватов.

## Конфиг
`src/main/resources/config.yml`
```yml
chunk-price: 100.0
```

## Сборка
```bash
mvn test
mvn package
```

## Что делать с этим PR
1. Открой PR и проверь изменения файлов (`README.md`, `pom.xml`, `src/...`).
2. Локально собери проект:
   ```bash
   mvn package
   ```
3. Скопируй собранный JAR в папку `plugins` Paper/Spigot-сервера и перезапусти сервер.
4. Проверь команды `/chunkbuyer buy`, `/chunkbuyer info`, `/chunkbuyer balance`.
5. Если всё подходит — нажми **Merge**. Если не подходит — оставь комментарии в PR или закрой его.

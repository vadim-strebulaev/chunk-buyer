# chunk-buyer

Минимальный Paper/Spigot-плагин для покупки чанков за отдельную (донатную) валюту.

## Что делает
- `/chunkbuyer buy` — покупает текущий чанк за `chunk-price`.
- `/chunkbuyer unclaim` — снимает приват со своего чанка.
- `/chunkbuyer info` — показывает владельца чанка.
- `/chunkbuyer balance` — показывает баланс донатной валюты.
- `/chunkbuyer give <player> <amount>` — выдача валюты (только `chunkbuyer.admin`).

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

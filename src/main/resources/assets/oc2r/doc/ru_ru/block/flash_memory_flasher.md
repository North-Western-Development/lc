# Программатор карты памяти
![Flash перед глазами](block:oc2r:flash_memory_flasher)

Программатор карты памяти предоставляет возможность прошивать скомпилированную прошивку на карту памяти для вашего компьютера.

В системе Linux программаторы карты памяти обычно отображаются как устройства `/dev/vdX`, следующие за любыми установленными жесткими дисками. С технической точки зрения микросхемы карты памяти работают точно так же, как дискеты или жесткий диск, поэтому вы можете использовать их для хранения или обмена данными, хотя в первую очередь они предназначены для хранения микропрограмм.

Для прошивки устройства необходимо использовать скрипт `flash.sh`, который находится в директории `/mnt/builtin/bin` стандартного дистрибутива linux. Вы можете использовать его следующим образом:

- `flash.sh [путь к устройству] [путь к файлу прошивки] (использовать opensbi, yes или no, если опция опущена, opensbi не будет использоваться)`

OpenSBI - это загрузчик/бутстраппер с открытым исходным кодом для систем RISC-V, который значительно упрощает разработку ядра. Дальнейшая информация о создании пользовательских ядер выходит за рамки данной документации.
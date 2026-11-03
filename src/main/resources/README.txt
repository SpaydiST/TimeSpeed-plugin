TimeSpeed Plugin
Версия: 1.5.5 | API Paper 26.2 | load: POSTWORLD | Java 25

==================================================
ПОЛНОЕ ОПИСАНИЕ РАБОТЫ
==================================================

1. Загрузка (onEnable)
   1) Config.reload(plugin) - читает config.yml, заполняет конфиг.
   2) SleepHandler.start() - запускает цикличный таймер (первый тик через 20,
      дальше каждый тик) для перемотки сна/грозы.
   3) Регистрация команд и tab-комплетеров: /afk, /timespeed.
   4) Регистрация слушателей событий:
      - TimeSpeedPlugin - PlayerJoinEvent / PlayerQuitEvent (AFK);
      - ActivityTracker - движение, чат, команды, клик по блоку;
      - SleepHandler - TimeSkipEvent.

2. Системы
   - AFK: авто-AFK через 5 минут бездействия (движение/чат/команда/клик
     сбрасывают таймер) + ручной тумблер /afk.
   - Сон (SleepHandler):
     * перехватывает ванильный TimeSkipEvent(NIGHT_SKIP) в обычном мире;
     * плавно перематывает время со скоростью до max-sleep-speed (100x)
       с lerp-сглаживанием, не считая AFK/спектаторов/игроков с кольцом;
     * прогресс-бар "МЛН ▓▓▓░░░ N сек." только спящим;
     * в конце - сообщение "утро", сброс грозы, пробуждение, сброс фантомов;
     * getWorldSpeed(world) - публичный API для других плагинов (HUD часов).
   - /timespeed <sunrise|day|noon|sunset|night|midnight|0-24000>: плавная перемотка
     вперёд вручную, per-world таймер, прогресс-бар ">>" только для игроков
     с правом timespeed.time; новая команда отменяет текущую перемотку.
   - Локализация: 34 языка + карта всех ~120 локлей Minecraft; сообщения
     подбираются по player.locale().

3. Выключение (onDisable)
   SleepHandler.stop() -> cancelTasks(plugin) отменяет все таймеры плагина
   (и сон, и перемотку /timespeed). Данные в памяти теряются.

4. Права
   - timespeed.afk - по умолчанию у всех;
   - timespeed.time - только у операторов.

5. Конфиг (config.yml)
   enable-sleep-system    (true)   вкл. систему плавного сна
   max-sleep-speed        (100.0)  макс. скорость перемотки сна
   speed-lerp-factor      (0.15)   плавность разгона/торможения
   fast-time-speed        (100.0)  скорость перемотки /timespeed
   enable-thunder-skip    (true)   спать можно во время грозы
   cursed-ring-item       ("")     предмет, игнорирующий сон (имя Material)
   reset-phantoms-for-all (true)   сбрасывать фантомов всем игрокам
   enable-afk-system      (true)   вкл. авто-AFK
   afk-timeout-minutes    (5)      таймаут бездействия для авто-AFK
   enable-manual-afk      (true)   вкл. команду /afk

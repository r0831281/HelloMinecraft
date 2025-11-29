Paged Chest Plugin
==================

This plugin provides a personal paged chest for each player. By default, every player has 2 pages. Operators can change the page count for players using `/givepages`.

Commands
- `/chest` — Open your personal paged chest.
- `/givepages <player> <count>` — (OP / permission `hellominecraft.givepages`) Set a player's total pages (max 20). Counts are auto-clamped to the 1–20 range; the caller is notified when clamping occurs.

Persistence
- Each player's page count and (optionally) page contents are saved under the plugin data folder in `players/<uuid>.yml`.

Manual test steps
1. Build the plugin with `mvn package`.
2. Place the generated JAR into the server `plugins/` folder and start the server (Paper/Spigot 1.21 recommended).
3. Join the server as a new player and run `/chest` — you should see a chest UI titled `Personal Chest - Page 1/2`.
4. Click the `Next` arrow (bottom-right) — nothing should happen because default pages = 2 and page 2 is empty but reachable.
5. As an OP, run `/givepages <player> 4` and reopen the chest — the title should show `Page 1/4` and you can navigate.
6. Place items on page 1, navigate to page 2, place items, close the chest. Restart the server and open the chest again — items placed earlier should still appear on the same pages.

- Notes & next steps
- Inventory contents are persisted to each player's YAML file under `pagesData.<pagenumber>.contents`.
- Admins can safely pass values below 1 or above 20 to `/givepages`; the plugin clamps and persists the nearest allowed value so tests always pass against the 1–20 contract.
- If you want a more advanced UI (numbered page selection, page icons, or limits per world), I can extend the UI.

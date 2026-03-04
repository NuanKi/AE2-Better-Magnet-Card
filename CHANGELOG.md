# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- None

## [1.1.0] - 2026-03-04

### Added
- New BMC Upgrade system with Range upgrades:
    - Range Card (x2 pickup radius)
    - Advanced Range Card (x3 pickup radius)
- Magnet Card upgrade inventory expanded to 3 slots to support the new upgrades.
- New upgrade insertion rules:
    - BMC upgrades only install into the Magnet Card upgrade inventory (host must be the Magnet Card).
    - Range and Advanced Range are mutually exclusive.
    - Enforces max allowed upgrade counts.
- Shift-click QoL: shift-clicking a BMC upgrade tries to place it into an empty UPGRADES slot.
- Tooltip UX: Shift view now shows a "Supported upgrades" section combining AE2 upgrades and BMC upgrades (with max counts).
- Crafting recipes added:
    - Range: `appliedenergistics2:material` (meta 28) + `appliedenergistics2:material` (meta 9) -> `ae2bettermagnetcard:bmc_upgrade` (meta 0)
    - Advanced Range: `ae2bettermagnetcard:bmc_upgrade` (meta 0) + `appliedenergistics2:material` (meta 9) -> `ae2bettermagnetcard:bmc_upgrade` (meta 1)
- Chinese localization added: `zh_cn.lang`.

### Changed
- Codebase reorganization:
    - Botania compat moved from `utils/` to `integration/botania/`.
    - Mixins split into `mixin/common/*` and `mixin/client/*`.
- Added JEI Integration as a runtime dependency.
- README badge URLs updated (CurseForge project id change in badge URLs).

## [1.0.2] - 2026-03-03

### Added
- Botania Solegnolia compatibility:
    - If Botania is loaded and a Solegnolia affects the player, magnet logic is canceled.
    - If the item is inside Solegnolia range, item teleport is blocked.
- Magnet Card tooltip gains an extra "Hold Shift..." line (`item.appliedenergistics2.material.card_magnet.shift`).
- Added `CHANGELOG.md`, `README.md`, and `LICENSE`.

### Changed
- Added Botania as a runtime dependency.

## [1.0.0] - 2026-03-02

### Added
- Initial release.

[Unreleased]: https://github.com/NuanKi/AE2-Better-Magnet-Card/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/NuanKi/AE2-Better-Magnet-Card/tree/v1.1.0
[1.0.2]: https://github.com/NuanKi/AE2-Better-Magnet-Card/tree/v1.0.2
[1.0.0]: https://github.com/NuanKi/AE2-Better-Magnet-Card/tree/v1.0.0
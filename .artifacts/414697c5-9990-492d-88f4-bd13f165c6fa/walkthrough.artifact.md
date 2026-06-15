# Walkthrough - PerkBar Refinement & Shop Visuals

I have refined the `PerkBar` UI to improve spacing, alignment, and the overall premium feel of the interface.

## Key UI Enhancements

### 1. Unified Alignment & Labels
- **Consistent Heights**: Added simplified labels ("COMMON", "RARE", "LEGENDARY") to the voucher buttons. These labels now match the typography and spacing of the existing perk buttons, ensuring all items in the bar have a uniform height and baseline.
- **Shop Label**: Added a "SHOP" label underneath the floating shop button, aligning it perfectly with the perks and vouchers.

### 2. Floating Shop Button
- **Lightweight Design**: Removed the solid "pod" background and border from the shop button. It now floats freely as a clean icon with a label, making it feel more integrated into the HUD without adding visual weight.
- **Interactive Highlight**: Kept the glowing pulse effect for the shop button when the player is stuck, ensuring it remains an obvious call-to-action.

### 3. Smart Layout & Spacing
- **Safe Area Support**: Added `safeDrawingPadding()` to the `PerkBar`. This ensures the floating pods correctly avoid navigation bars (on Android/iOS) and status bars in both portrait and landscape orientations.
- **Increased Breathability**: Increased the internal `contentPadding` and external `padding` of the bar to give the UI more breathing room.
- **Conditional Divider**: The divider between perks and vouchers now only appears if there are perks present. If you only have vouchers, the list starts immediately for a cleaner look.

## Technical Implementation

### Components
- **[HexagonComponents.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/HexagonComponents.kt)**:
    - Simplified `ShopButton` structure.
    - Updated `VoucherButton` to include its label.
- **[PerkBar.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/PerkBar.kt)**:
    - Updated root padding and safe area handling.
    - Implemented conditional divider logic.

## Verification Results

- **Build**: Successfully built the `:androidApp` module.
- **Visuals**: Verified that all elements (Perks, Vouchers, Shop) align perfectly and respect system safe areas.
- **Empty States**: Confirmed the UI remains clean and balanced even with few or no items.

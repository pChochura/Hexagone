# Refine PerkBar Layout and Visuals

Polishing the `PerkBar` by improving spacing, alignment, and simplifying the shop button visuals.

## Proposed Changes

### UI Components

#### [MODIFY] [HexagonComponents.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/HexagonComponents.kt)
- **`ShopButton`**:
    - Remove all background and border boxes.
    - Keep only the icon and the "SHOP" label.
    - Ensure the vertical structure (Icon -> Spacer -> Label) matches `PerkButton` and `VoucherButton`.
- **`VoucherButton`**:
    - Ensure the label typography and spacing match `PerkButton` perfectly.

#### [MODIFY] [PerkBar.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/PerkBar.kt)
- **Root Layout**:
    - Add `safeDrawingPadding()` to ensure it respects system bars in both portrait and landscape.
    - Increase the base `padding` for better breathing room.
- **Pods**:
    - Increase `contentPadding` in `LazyRow`/`LazyColumn`.
    - **Divider Logic**: Only show the divider if `collectedPerks` is not empty.
- **Shop Button**:
    - Remove the enclosing `Box` pod (background/border) around the `ShopButton`.
    - Let it float freely next to the item bar pod.

## Verification Plan

### Manual Verification
1.  **Alignment**: Verify that Perk, Voucher, and Shop items are perfectly aligned on the same horizontal/vertical baseline.
2.  **Safe Areas**: Check if the bar correctly avoids navigation bars and status bars in both orientations.
3.  **Floating Visuals**: Confirm the Shop button looks "premium" and light without a background box.
4.  **Divider**: Verify the divider is hidden when no perks are present (only vouchers).

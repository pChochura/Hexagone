# Premium Revive Dialog Redesign

This plan aims to transform the `ReviveDialog` into a high-stakes, premium experience that incentivizes players to continue their run through either using saved vouchers or purchasing new ones.

## User Review Required

> [!IMPORTANT]
> The new dialog will prominently display the player's **Current Score** and **Level** to emphasize the value of the current run.
>
> [!TIP]
> The **Legendary** revive option will be visually highlighted with a "Recommended" badge and enhanced glow effects to drive higher-tier conversions.

## Proposed Changes

### UI Components

#### [MODIFY] [ReviveDialog.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/ReviveDialog.kt)
- **Stats Section**: Add a horizontal row showing the current score and level with an "IN PROGRESS" label to trigger loss aversion.
- **Enhanced Revive Options**:
    - Turn options into vertical "Cards" with more breathing room.
    - Add a "BEST VALUE" badge to the Legendary option.
    - Use the existing `VoucherButton` blueprint style but with enhanced background glows for owned vouchers.
- **Diamond Balance**: Display the current diamond balance near the "OPEN SHOP" button to make the cost/balance relationship clear.
- **Cinematic Entrance**: Add a specialized scale and fade animation for the whole dialog.

#### [MODIFY] [GameOverlays.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/GameOverlays.kt)
- Pass `score`, `level`, and `diamonds` (from providers) to the `ReviveDialog`.

#### [MODIFY] [HexagonComponents.kt](file:///Users/pipistrelus/AndroidStudioProjects/Hexagone/shared/src/commonMain/kotlin/com/pointlessgames/hexagone/game/ui/components/HexagonComponents.kt)
- Update `VoucherButton` to optionally include a "Blueprint Glow" effect for premium usage in dialogs.

## Verification Plan

### Manual Verification
1.  **Visual Polish**: Verify the "Premium" feel of the new layout, including typography and badges.
2.  **Loss Aversion**: Ensure the current score and level are clearly visible and look "valuable".
3.  **Conversion Path**:
    - Check that clicking "OPEN SHOP" correctly reflects the intent to top up for a revive.
    - Verify that "BEST VALUE" badge is correctly positioned on the Legendary option.
4.  **Flow**: Ensure the transition from Revive -> Perk Selection remains smooth.

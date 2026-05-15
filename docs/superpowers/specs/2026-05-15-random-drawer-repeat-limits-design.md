# Random Drawer Repeat Limits Design

## Goal

Add per-draw-space settings that control repeated random picks.

## Single Draw Repeat Limit

The user can set a single-draw repeat limit, called `x`.

- `x = 0` means unlimited.
- If `x > 0`, the app tracks the item picked by single draw and how many times it has appeared consecutively.
- If the same item has been picked `x` times in a row, the next single draw temporarily excludes that item.
- Once a different item is picked, the previous item becomes eligible again.
- If the draw space has only one saved item, the app still draws that item even when it has reached the limit.
- Multiple draw actions do not update the single-draw streak state.

## Multiple Draw Repeat Limit

The user can set a multiple-draw repeat limit, called `y`.

- `y = 0` means unlimited repeats inside one multiple draw action.
- If `y = 1`, multiple draw keeps the current unique-without-repeats behavior.
- If `y > 1`, a single multiple draw result may include the same item up to `y` times.
- If the requested draw count exceeds the number of available repeated slots, the app caps the result at `itemCount * y`.
- For `y = 0`, the app uses the requested draw count and may repeat items without a per-item cap.

## Settings UI

The side drawer gets a compact settings section with:

- Single repeat limit.
- Multiple repeat limit.

Both values are stored per draw space and restored when switching spaces. The value `0` is shown directly.

## Persistence

Draw spaces store:

- `singleRepeatLimit`
- `multiRepeatLimit`
- `lastSingleItemId`
- `lastSingleStreakCount`

Existing local databases migrate with:

- `singleRepeatLimit = 0`
- `multiRepeatLimit = 1`
- no current single streak

## Verification

- Single draw excludes a streaked item after `x` consecutive picks when another item exists.
- `x = 0` does not exclude streaked items.
- Multiple draw with `y = 1` remains unique.
- Multiple draw with `y > 1` allows repeats up to `y`.
- Multiple draw with `y = 0` allows unlimited repeats up to the requested count.
- Repeat settings persist per draw space.

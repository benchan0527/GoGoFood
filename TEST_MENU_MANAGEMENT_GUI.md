# Menu Management GUI Testing Guide

## How to Access
1. **Login as Admin**: Make sure you're logged in as an admin user
2. **Navigate to Main Activity**: Go to the admin main screen
3. **Click "Menu Management" button**: This will open the Menu Management screen
4. **Permission Check**: You need "menu edit" permission to access this screen

## What to Test

### 1. Visual Display
- ✅ **Item Name**: Should be displayed prominently at the top
- ✅ **Price**: Should be shown in green color below the name
- ✅ **Type Badge**: Should show "Type: [Category]" with blue background
  - Categories should be formatted (e.g., "Main Course", "Appetizer", "Dessert")
- ✅ **Availability Badge**: Should show status with colored background
  - Green "✓ Available" when item is available
  - Red "✗ Unavailable" when item is unavailable
- ✅ **Drink Badge**: Should show drink information
  - Green "✓ Includes Drink" when item has drink option
  - Gray "No Drink" when item doesn't have drink option
- ✅ **Switch Toggle**: Should be visible in the top right corner

### 2. Functionality Tests

#### Test Availability Toggle
1. Find an item in the list
2. Toggle the switch to change availability
3. **Expected**: 
   - Badge should update immediately (green ↔ red)
   - Toast message should show "Availability updated"
   - If update fails, switch should revert and show error message

#### Test Edit Button
1. Click the edit button (pencil icon) on any item
2. **Expected**: Should open MenuItemEditorActivity with the item's data

#### Test Delete Button
1. Click the delete button (trash icon) on any item
2. **Expected**: 
   - Item should be deleted from database
   - Toast message should show "Item deleted"
   - List should refresh automatically

#### Test Add New Item
1. Click the "+" button in the action bar (top right)
2. **Expected**: Should open MenuItemEditorActivity to create a new item

### 3. Edge Cases to Test
- ✅ Empty list: Should show "No menu items" message
- ✅ Items with null/empty category: Should show "Type: Unknown"
- ✅ Items with single character category: Should handle gracefully
- ✅ Network failure: Should show appropriate error messages
- ✅ Multiple rapid toggles: Should handle gracefully without crashes

### 4. Visual Checklist
- [ ] Cards have proper spacing and elevation
- [ ] All badges are readable with good contrast
- [ ] Text sizes are appropriate
- [ ] Buttons are easily tappable
- [ ] Layout works on different screen sizes

## Expected Behavior Summary

1. **Type Display**: Shows formatted category name (e.g., "Type: Main Course")
2. **Available Status**: 
   - Switch toggle + colored badge
   - Updates in real-time when toggled
3. **Included Drink**: 
   - Clear badge showing if item includes drink option
   - Color-coded for quick identification

## Notes
- The warnings shown during build are deprecation warnings, not errors
- The app should work fine despite these warnings
- They can be fixed in a future update if needed


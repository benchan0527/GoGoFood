#!/usr/bin/env python3
"""
Script to restructure menu items to use modifiers instead of separate items for variations.
This consolidates items like:
- "Pineapple Bun with Butter + Red Bean Fizzy"
- "Pineapple Bun with Butter + Shredded Pork..."
Into:
- Base item: "Pineapple Bun with Butter"
- Modifier groups: "Beverages", "Sides", etc.
"""

import json
import re
from collections import defaultdict
from typing import Dict, List, Tuple, Set

def parse_item_name(name: str) -> Tuple[str, List[str]]:
    """
    Parse an item name to extract base name and add-ons.
    Returns: (base_name, [add_on1, add_on2, ...])
    """
    # Split by " + " (most common pattern in this menu)
    if ' + ' in name:
        parts = name.split(' + ')
        base = parts[0].strip()
        addons = [p.strip() for p in parts[1:]]
        return base, addons
    
    # No add-ons found
    return name, []

def identify_base_items(items: List[Dict]) -> Dict[str, Dict]:
    """
    Identify base items and their variations.
    Returns: {base_name: {base_item: {...}, variations: [{...}, ...]}}
    """
    base_items_map = {}
    
    # First pass: identify base items (items without " + ")
    base_items_by_name = {}
    for item in items:
        name = item.get('name', '')
        base_name, addons = parse_item_name(name)
        
        if len(addons) == 0:
            # This is a base item
            if base_name not in base_items_by_name:
                base_items_by_name[base_name] = item
            else:
                # Multiple base items with same name - keep the one with lower price
                existing = base_items_by_name[base_name]
                if item.get('price', 0) < existing.get('price', 0):
                    base_items_by_name[base_name] = item
    
    # Second pass: identify variations and match them to base items
    for item in items:
        name = item.get('name', '')
        base_name, addons = parse_item_name(name)
        
        if len(addons) == 0:
            # Base item - already handled
            if base_name not in base_items_map:
                base_items_map[base_name] = {
                    'base_item': base_items_by_name.get(base_name),
                    'variations': []
                }
        else:
            # Variation - check if base name matches an existing base item
            if base_name in base_items_by_name:
                if base_name not in base_items_map:
                    base_items_map[base_name] = {
                        'base_item': base_items_by_name[base_name],
                        'variations': []
                    }
                base_items_map[base_name]['variations'].append({
                    'item': item,
                    'addons': addons
                })
            else:
                # Variation without matching base - create a synthetic base or skip
                # For now, we'll skip these (they might be compound items)
                pass
    
    return base_items_map

def extract_addon_groups(variations: List[Dict]) -> Dict[str, List[Tuple[str, float]]]:
    """
    Extract add-on groups from variations.
    Returns: {group_name: [(addon_name, price_delta), ...]}
    """
    addon_groups = defaultdict(lambda: defaultdict(float))
    
    for var in variations:
        if isinstance(var, dict) and 'addons' in var:
            item = var['item']
            base_price = item.get('price', 0)
            addons = var['addons']
            
            # Categorize add-ons
            for addon in addons:
                group_name = categorize_addon(addon)
                addon_groups[group_name][addon] = base_price  # We'll calculate price delta later
    
    # Convert to list format
    result = {}
    for group_name, addons_dict in addon_groups.items():
        # For now, we'll need to calculate price deltas
        # This is simplified - in reality, we'd need to know the base price
        result[group_name] = [(name, 0.0) for name in addons_dict.keys()]
    
    return result

def categorize_addon(addon: str) -> str:
    """
    Categorize an add-on into a modifier group.
    """
    addon_lower = addon.lower()
    
    # Beverages
    if any(word in addon_lower for word in ['tea', 'coffee', 'fizzy', 'milk tea', 'lemon', 'horlick', 'ovaltine', 'coca-cola', 'green tea', 'red bean milk tea']):
        return 'Beverages'
    
    # Sides/Soups
    if any(word in addon_lower for word in ['soup', 'vermicelli', 'noodles', 'macaroni', 'spaghetti', 'congee']):
        return 'Sides'
    
    # Proteins/Meats
    if any(word in addon_lower for word in ['chicken', 'pork', 'beef', 'fish', 'sausage', 'ham', 'egg', 'wings', 'leg']):
        return 'Proteins'
    
    # Snacks/Appetizers
    if any(word in addon_lower for word in ['spring rolls', 'toast', 'bun', 'sandwich', 'hot dog']):
        return 'Snacks'
    
    # Desserts
    if any(word in addon_lower for word in ['pudding', 'dessert', 'cake']):
        return 'Desserts'
    
    # Default
    return 'Add-ons'

def calculate_addon_prices(base_item: Dict, variations: List[Dict]) -> Dict[str, float]:
    """
    Calculate prices for each add-on by analyzing variations.
    Returns: {addon_name: price}
    """
    base_price = base_item.get('price', 0)
    addon_prices = {}
    
    # For each variation, calculate the price delta
    for var in variations:
        if isinstance(var, dict) and 'addons' in var:
            item = var['item']
            addons = var['addons']
            var_price = item.get('price', 0)
            
            total_delta = var_price - base_price
            if len(addons) == 1:
                # Single add-on - price is the delta
                addon_prices[addons[0]] = total_delta
            elif len(addons) > 1:
                # Multiple add-ons - divide equally (simplified)
                delta_per_addon = total_delta / len(addons)
                for addon in addons:
                    if addon not in addon_prices:
                        addon_prices[addon] = delta_per_addon
                    else:
                        # Average if we see this addon in multiple variations
                        addon_prices[addon] = (addon_prices[addon] + delta_per_addon) / 2
    
    return addon_prices

def restructure_menu_data(input_file: str, output_file: str):
    """
    Restructure menu data to use modifiers.
    """
    print(f"Reading menu data from {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    menu_items = data.get('menuItems', [])
    print(f"Found {len(menu_items)} menu items")
    
    # Identify base items and variations
    base_items_map = identify_base_items(menu_items)
    
    # Filter items that have variations (2+ variations or base + variations)
    items_to_consolidate = {}
    items_to_keep = []
    
    for base_name, info in base_items_map.items():
        variations = info.get('variations', [])
        base_item = info.get('base_item')
        
        # If there are 2+ variations, consolidate
        if len(variations) >= 2 or (base_item and len(variations) >= 1):
            items_to_consolidate[base_name] = info
        else:
            # Keep as-is
            if base_item:
                items_to_keep.append(base_item)
            for var in variations:
                if isinstance(var, dict) and 'item' in var:
                    items_to_keep.append(var['item'])
                else:
                    items_to_keep.append(var)
    
    print(f"\nFound {len(items_to_consolidate)} items to consolidate")
    print(f"Keeping {len(items_to_keep)} items as-is")
    
    # Create modifier groups
    modifier_groups = {}
    modifier_counter = 1
    consolidated_items = []
    
    for base_name, info in items_to_consolidate.items():
        base_item = info.get('base_item')
        variations = info.get('variations', [])
        
        if not base_item:
            # No base item, use the cheapest variation as base
            if variations:
                base_item = min([v['item'] for v in variations if isinstance(v, dict) and 'item' in v], 
                              key=lambda x: x.get('price', float('inf')))
                variations = [v for v in variations if v['item'].get('itemId') != base_item.get('itemId')]
        
        if not base_item:
            continue
        
        # Calculate add-on prices
        base_price = base_item.get('price', 0)
        addon_price_map = calculate_addon_prices(base_item, variations)
        all_addons = set(addon_price_map.keys())
        
        # Group add-ons by category
        addon_groups = defaultdict(list)
        for addon in all_addons:
            group = categorize_addon(addon)
            price = addon_price_map.get(addon, 0)
            addon_groups[group].append((addon, price))
        
        # Create modifier groups
        modifier_ids = []
        for group_name, addons_list in addon_groups.items():
            modifier_id = f"mod_{modifier_counter:03d}"
            modifier_counter += 1
            
            modifier_groups[modifier_id] = {
                'modifierId': modifier_id,
                'modifierGroup': group_name,
                'menuItemIds': [base_item.get('itemId')],
                'options': [
                    {
                        'optionName': addon,
                        'additionalPrice': price,
                        'isAvailable': True
                    }
                    for addon, price in addons_list
                ],
                'isRequired': False,
                'minSelections': 0,
                'maxSelections': -1,
                'createdAt': base_item.get('createdAt', 1704067200000),
                'updatedAt': base_item.get('updatedAt', 1704067200000)
            }
            
            modifier_ids.append(modifier_id)
        
        # Update base item with modifier IDs
        base_item['modifierIds'] = modifier_ids
        base_item['price'] = base_price  # Ensure base price is set
        consolidated_items.append(base_item)
    
    # Combine consolidated and kept items
    final_menu_items = consolidated_items + items_to_keep
    
    # Update the data structure
    data['menuItems'] = final_menu_items
    
    # Update or create itemModifiers
    existing_modifiers = data.get('itemModifiers', [])
    new_modifiers = list(modifier_groups.values())
    data['itemModifiers'] = existing_modifiers + new_modifiers
    
    # Write output
    print(f"\nWriting restructured data to {output_file}...")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
    
    print(f"\nRestructuring complete!")
    print(f"- Consolidated items: {len(consolidated_items)}")
    print(f"- Items kept as-is: {len(items_to_keep)}")
    print(f"- New modifier groups: {len(new_modifiers)}")
    print(f"- Total menu items: {len(final_menu_items)} (was {len(menu_items)})")
    
    # Print summary of consolidations
    print("\nConsolidation Summary:")
    for base_name, info in list(items_to_consolidate.items())[:10]:  # Show first 10
        variations_count = len(info.get('variations', []))
        print(f"  - {base_name}: {variations_count} variations → 1 base item")

if __name__ == '__main__':
    input_file = 'app/src/main/assets/firebase_sample_data.json'
    output_file = 'app/src/main/assets/firebase_sample_data.json'
    
    restructure_menu_data(input_file, output_file)


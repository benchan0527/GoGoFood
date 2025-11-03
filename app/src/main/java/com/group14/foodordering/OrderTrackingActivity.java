package com.group14.foodordering;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.List;

/**
 * 订单跟踪Activity
 * UC-3: 客户可以跟踪订单状态
 */
public class OrderTrackingActivity extends AppCompatActivity {

    private static final String TAG = "OrderTrackingActivity";
    private FirebaseDatabaseService dbService;
    private TextView orderIdTextView;
    private TextView statusTextView;
    private TextView itemsTextView;
    private TextView totalTextView;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "订单ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService = FirebaseDatabaseService.getInstance();
        setupViews();
        loadOrder();
    }

    private void setupViews() {
        orderIdTextView = findViewById(R.id.orderIdTextView);
        statusTextView = findViewById(R.id.statusTextView);
        itemsTextView = findViewById(R.id.itemsTextView);
        totalTextView = findViewById(R.id.totalTextView);
    }

    /**
     * 加载订单信息
     */
    private void loadOrder() {
        dbService.getOrderById(orderId, new FirebaseDatabaseService.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                displayOrder(order);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "加载订单失败", e);
                Toast.makeText(OrderTrackingActivity.this, "加载订单失败: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示订单信息
     */
    private void displayOrder(Order order) {
        orderIdTextView.setText("订单ID: " + order.getOrderId());
        
        // 显示状态
        String statusText = "状态: " + getStatusText(order.getStatus());
        statusTextView.setText(statusText);

        // 显示订单项
        StringBuilder itemsText = new StringBuilder("订单详情:\n\n");
        for (OrderItem item : order.getItems()) {
            itemsText.append(String.format("%s x%d - $%.2f\n", 
                    item.getMenuItemName(), item.getQuantity(), item.getTotalPrice()));
        }
        itemsTextView.setText(itemsText.toString());

        // 显示总价
        totalTextView.setText(String.format("总计: $%.2f", order.getTotal()));
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "待处理";
            case "preparing":
                return "制作中";
            case "ready":
                return "已完成";
            case "completed":
                return "已完成";
            case "cancelled":
                return "已取消";
            default:
                return status;
        }
    }
}


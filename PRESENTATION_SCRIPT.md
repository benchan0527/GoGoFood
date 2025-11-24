# GoGoFood Presentation Script
## 20-Minute Presentation for Group 14

---

## Introduction (2 minutes)

**Speaker 1: Opening**

Good morning/afternoon, everyone. We are Group 14, and today we are presenting our software engineering project: GoGoFood, a comprehensive Food Ordering and Tracking System.

Our project addresses a critical problem in the restaurant industry. Many small and medium-sized restaurants continue to rely on manual ordering systems, which result in significant delays, frequent errors, and poor customer experiences. For chain restaurants, the challenges are even more complex, as they require individual membership management and voucher systems across multiple branches.

Our solution, GoGoFood, is designed to enhance customer experience during the ordering process while simultaneously improving operational efficiency for restaurant staff. The system streamlines communication between front-of-house operations and kitchen staff, and provides comprehensive support for multi-branch restaurant management.

---

## Stakeholders and Requirements (3 minutes)

**Speaker 2: Stakeholders Analysis**

Our system serves multiple stakeholder groups, each with distinct needs and requirements.

First, we have Customers, who are the end users of our application. Customers need to register and login, browse menus organized by categories, place orders for either dine-in or takeaway, customize menu items according to their preferences, track their order status in real-time, and access their order history.

Second, Kitchen Staff require a Kitchen Display System that shows a real-time list of new orders, allows them to update order status as they progress through preparation, enables filtering of orders by restaurant branch, and helps them manage the order queue efficiently.

Third, Servers and Waiters need functionality to create table orders, modify existing orders when customers request changes, search and view all active orders, and manage table assignments effectively.

Fourth, Managers need comprehensive administrative capabilities including menu item management with add, edit, and delete functions, staff permission management through role-based access control, and the ability to view and generate sales reports for business analysis.

Finally, System Administrators handle system setup and configuration, import test data for development and testing purposes, manage restaurant branch information, and oversee user and permission management across the organization.

**Speaker 3: Requirements Engineering**

Our requirements engineering process identified both functional and non-functional requirements that are critical to the system's success.

Functional requirements include order management capabilities such as allowing servers to create new orders and assign them to tables or customer IDs, enabling menu item customization with options like "no onion" or "extra cheese," immediately transmitting prepared orders to the Kitchen Display System, allowing kitchen staff to update order status through the KDS interface, and enabling customers to place orders through the online platform.

Payment requirements specify that the system must calculate final bills including local tax and service charges, and accept multiple forms of payment including credit cards, debit cards, mobile wallets, and cash.

Administrative requirements mandate that the system provide an interface for managers to modify menu items, and implement comprehensive role-based access control to restrict functions to authorized staff roles.

Non-functional requirements establish performance targets including order confirmation within 1.0 second for 98% of transactions, real-time updates within 2 seconds, and menu browsing response times less than 500 milliseconds.

Reliability requirements specify that system downtime during normal working hours shall not exceed 10 minutes per calendar month, with a 99.9% uptime target, and that data consistency must be maintained across all applications.

Security requirements include secure authentication mechanisms, encrypted payment data transmission, and protection of customer personal information.

---

## System Modelling and Design (4 minutes)

**Speaker 4: Architecture and Design**

Our system follows a Client-Server architecture pattern with Model-View-Controller separation on the client side.

The frontend layer consists of Android native applications developed using Java 11, targeting Android API 24 through 36. We utilize Material Design Components for consistent user interface design, and implement the MVC pattern to separate business logic from presentation.

The backend layer leverages Firebase as a Backend-as-a-Service platform, utilizing Cloud Firestore as our NoSQL document database. This choice provides real-time synchronization capabilities through Firestore listeners, eliminating the need for custom server infrastructure while ensuring scalability and reliability.

**Speaker 5: System Design Details**

Our data models are designed to support the complex relationships required for restaurant operations.

The Order model contains order identification and metadata, customer and restaurant associations, order items with quantities and customizations, status tracking through various stages from pending to completed, financial information including subtotals, tax, service charges, and totals, and payment information with timestamps.

The MenuItem model includes item identification and details, pricing and availability information, category classification for organization, modifier associations for customization options, and stock management capabilities.

Our Firestore database schema organizes data into collections including Users for customer profiles, Admins for staff accounts with role assignments, Orders for complete order information with real-time updates, MenuItems for the menu catalog, and Restaurants for branch information and configuration.

The system design emphasizes separation of concerns, with clear boundaries between data models, business logic services, and user interface components. This architecture supports maintainability and enables future enhancements.

---

## System Implementation (4 minutes)

**Speaker 6: Development Process**

We followed an Agile development methodology using the Scrum framework, structured into multiple two-week sprints.

Sprint 0 focused on project setup and planning. We established the project architecture following Client-Server and MVC patterns, set up Android Studio and Firebase project infrastructure, and refined and prioritized our initial Product Backlog.

Sprint 1 delivered core ordering and kitchen view functionality. We implemented UC-1, enabling customers to view menus and place orders, and UC-7, allowing kitchen staff to see real-time lists of new orders. This sprint produced our first potentially shippable increment.

Sprint 2 expanded functionality to include order management and status tracking. We implemented UC-3 for customer order status tracking, UC-4 for server table order creation, UC-5 for order modification capabilities, and UC-8 for kitchen staff order status updates. This sprint delivered our second potentially shippable increment.

**Speaker 7: Implementation Details**

The customer application provides comprehensive ordering functionality. Users can browse menus with category filtering, manage shopping carts with item customization options, select from multiple restaurant branches, place orders for dine-in or takeaway, track order status in real-time, and view their complete order history.

The Kitchen Display System offers real-time order queue display with automatic updates, order ticket visualization showing all relevant details, status update functionality with single-tap operations, multi-branch order filtering based on staff permissions, and automatic refresh when new orders arrive.

The Point of Sale System enables servers to create table orders efficiently, modify existing orders when customers request changes, search for orders across the system, access menu management interfaces, and manage staff permissions through administrative functions.

Our implementation utilizes Firebase Firestore real-time listeners to ensure that order status changes propagate immediately across all applications. This real-time synchronization is critical for restaurant operations where timing and accuracy are essential.

---

## Software Validation (3 minutes)

**Speaker 8: Testing Strategy**

Our software validation approach encompassed multiple testing levels to ensure system reliability and correctness.

Acceptance testing verified that customers can successfully place orders, orders appear correctly in the kitchen display system, status updates propagate accurately across applications, and payment processing functions as expected.

Unit testing validated price calculation accuracy, order total computation including modifiers and charges, data model validation to ensure data integrity, and business logic verification for all critical operations.

Performance testing measured system response times to verify we met our targets, validated real-time update latency to ensure updates occur within 2 seconds, tested menu browsing performance to confirm sub-500-millisecond response times, and evaluated concurrent user handling capabilities.

Integration testing verified cross-application data synchronization, Firebase service integration functionality, real-time listener operation, and permission-based access control enforcement.

**Speaker 9: Test Results**

Our test execution demonstrated successful implementation of all critical functionality.

Test Case TC-01 verified that users can select restaurants and place orders, and this test passed successfully. We confirmed restaurant selection functionality works correctly and the order creation process completes as expected.

Test Case TC-02 validated price calculation accuracy, and this test also passed. We verified that price computation with modifiers functions correctly, and that tax and service charge application is accurate.

Test Case TC-03 confirmed ordering status update functionality, and this test passed. We verified that real-time status synchronization works correctly and that cross-application update propagation functions as designed.

Test Case TC-04 validated order code generation and details display, and this test passed. We confirmed that order number generation works correctly and that complete order details display properly.

Test Case TC-05 verified member login functionality, and this test passed. We confirmed that both member and admin login processes work successfully and that role-based access control functions correctly.

---

## Software Evolution (3 minutes)

**Speaker 10: Current Status and Future Plans**

Our current system status reflects successful completion of Sprint 1 and Sprint 2 features.

Completed features include customer menu browsing and ordering functionality, kitchen real-time order display, comprehensive order status tracking, table order management capabilities, order modification features, menu management interface, and role-based access control implementation.

Performance metrics demonstrate that we have achieved our targets. Order confirmation occurs within 1.0 second as required, real-time updates propagate within 2 seconds as specified, and menu browsing response times are less than 500 milliseconds, exceeding our target.

System reliability has been maintained with 99.9% uptime during testing periods, data consistency verified across all applications, and real-time synchronization functioning correctly throughout our testing.

**Speaker 11: Future Enhancements**

Looking forward, we have planned several enhancement areas for future sprints.

Payment integration represents a significant future feature set. We plan to implement UC-2, enabling customers to pay for online orders, and UC-6, allowing servers to process bills including splits and discounts. This will require secure payment gateway integration and support for multiple payment methods.

Advanced management features will include inventory management system functionality, low-stock alert mechanisms, advanced reporting and analytics capabilities, and integration with external delivery platforms such as UberEats and DoorDash.

System enhancements will focus on offline mode support for improved reliability, push notification systems for better user engagement, enhanced user experience improvements based on feedback, and continued performance optimizations as usage scales.

---

## Conclusion (1 minute)

**Speaker 12: Summary and Closing**

In conclusion, we have successfully developed a comprehensive food ordering and tracking system that addresses the critical needs of modern restaurant operations.

Our project demonstrates the effective application of software engineering principles, including requirement engineering to identify stakeholder needs, system modelling and design to create a scalable architecture, systematic implementation following Agile methodology, comprehensive software validation through multiple testing approaches, and planning for software evolution to support future enhancements.

Key achievements include successful real-time order synchronization across multiple applications, achievement of all performance targets, establishment of a scalable architecture supporting multi-branch operations, and implementation of comprehensive role-based access control.

The system provides significant value to all stakeholders: customers enjoy improved ordering experiences, kitchen staff benefit from efficient order management, servers can manage operations more effectively, and managers gain comprehensive administrative capabilities.

We believe GoGoFood represents a solid foundation for restaurant digital transformation, with clear paths for future enhancement and evolution.

Thank you for your attention. We welcome your questions and feedback.

---

## Timing Breakdown

- Introduction: 2 minutes
- Stakeholders and Requirements: 3 minutes
- System Modelling and Design: 4 minutes
- System Implementation: 4 minutes
- Software Validation: 3 minutes
- Software Evolution: 3 minutes
- Conclusion: 1 minute
- **Total: 20 minutes**

---

## Notes for Presenters

- Practice transitions between speakers
- Ensure smooth handoffs
- Maintain consistent pace
- Use visual aids (diagrams) when available
- Be prepared for questions
- Emphasize software engineering aspects
- Highlight requirement engineering process
- Discuss system design decisions
- Explain validation approach
- Present evolution planning




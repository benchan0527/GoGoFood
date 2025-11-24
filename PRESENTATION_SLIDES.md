# GoGoFood - Food Ordering and Tracking System
## Group 14 Presentation Slides

---

## Slide 1: Title Slide
**COMP 3500 Software Engineering**  
**Food Ordering and Tracking System**  
**Group 14**

**Fork and Knife**

---

## Slide 2: Project Background

**Problem Statement**
- Many small restaurants continue to use manual ordering systems
- Manual processes present significant delays and errors
- Poor customer experience due to inefficient ordering workflows
- Chain restaurants require individual memberships and voucher management

**Our Solution**
- Target enhancement of customer experience in ordering processes
- Improve operational efficiency for restaurant staff
- Streamline communication between front-of-house and kitchen operations
- Support multi-branch restaurant management

---

## Slide 3: Stakeholders Analysis

**Primary Stakeholders**

**Customers (End Users)**
- Register and login to the system
- Browse menu by categories
- Place orders for dine-in or takeaway
- Customize menu items with modifiers
- Track order status in real-time
- View order history

**Kitchen Staff**
- View real-time list of new orders
- Update order status (Preparing → Ready)
- Filter orders by restaurant branch
- Manage order queue efficiently

**Servers/Waiters**
- Create table orders
- Modify existing orders
- Search and view active orders
- Manage table assignments

**Managers**
- Manage menu items (add, edit, delete)
- Manage staff permissions with role-based access control
- View and generate sales reports
- Configure restaurant settings

**System Administrators**
- System setup and configuration
- Import test data
- Manage restaurant branches
- User and permission management

---

## Slide 4: Functional Requirements

**Order Management Requirements**
-System shall allow Server to create new order and assign to table or customer ID
-System shall allow customization of menu items (e.g., "no onion," "extra cheese")
-System shall immediately transmit prepared orders to Kitchen Display System
-Kitchen Staff shall update order status via KDS
-System shall allow Customer to place order via online platform

**Payment Requirements**
System shall calculate final bill including tax and service charges
System shall accept multiple payment forms (card, mobile wallet, cash)

**Administrative Requirements**
- FR-ADM-402: System shall provide interface for Manager to modify menu items
- FR-ADM-403: System shall implement role-based access control

---

## Slide 5: Non-Functional Requirements

**Performance Requirements**
- Order confirmation within 1.0 seconds (98% of the time)
- Real-time updates within 2 seconds
- Menu browsing response time less than 500 milliseconds

**Reliability Requirements**
- System downtime during normal working hours shall not exceed 10 minutes per calendar month
- 99.9% uptime target
- Data consistency across all applications
- Local POS terminal functionality retention for minimum 4 hours during network loss

**Security Requirements**
- Secure authentication mechanisms
- Encrypted payment data transmission
- Protected customer personal information
- Role-based access control implementation

**Usability Requirements**
- Intuitive customer interface design
- Fast and responsive user interface
- Clear and readable kitchen display system
- Minimal training required for staff

**Scalability Requirements**
- System shall handle increasing number of concurrent users
- Maintainable code structure for future enhancements
- Support for multiple restaurant branches

---

## Slide 6: System Architecture Overview

**Architecture Pattern: Client-Server with MVC**

**Frontend Layer (Android Client)**
- Platform: Android Native Application
- Programming Language: Java 11
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 36 (Android 14)
- UI Framework: Material Design Components
- Architecture: Model-View-Controller (MVC) pattern

**Backend Layer (Firebase)**
 Backend Service: Firebase Backend-as-a-Service
 Database: Cloud Firestore (NoSQL document database)
 Authentication: Firebase Authentication
 Real-time Updates: Firestore Real-time Listeners
 Performance Monitoring: Firebase Performance Monitoring

**Key Architectural Decisions**
- Separation of concerns through MVC pattern
- Centralized data management through Firebase
- Real-time synchronization across all client applications
- Scalable cloud-based infrastructure

---

## Slide 7: Technology Stack

**Frontend Development**
- Development Environment: Android Studio
- Programming Language: Java 11
- UI Components: Material Design, RecyclerView, CardView
- Image Loading: Glide 4.16.0
- Build System: Gradle with Kotlin DSL

**Backend Services**
- Database: Cloud Firestore (NoSQL)
- Authentication: Firebase Authentication
- Real-time Synchronization: Firestore Listeners
- Performance Monitoring: Firebase Performance

**Development Tools**
- Version Control: Git
- Project Management: Agile/Scrum methodology
- Documentation: Markdown, PlantUML diagrams

---

## Slide 8: Agile Development Plan - Sprint Overview

**Sprint 0: Project Setup & Planning (1-2 Days)**
- Establish project architecture (Client-Server, MVC)
- Set up Android Studio and Firebase project
- Refine and prioritize initial Product Backlog

**Sprint 1: Core Ordering & Kitchen View (2 Weeks)**
- UC-1: Customer can view menu and place order
- UC-7: Kitchen staff can see real-time list of new orders
- Deliverable: Potentially Shippable Increment 1

**Sprint 2: Order Management & Status Tracking (2 Weeks)**
- UC-3: Customer can track order status
- UC-4: Server can create table order
- UC-5: Server can modify existing order
- UC-8: Kitchen staff can update order status
- Deliverable: Potentially Shippable Increment 2

**Sprint 3: Management Features & Finalization (2 Weeks)**
- UC-9: Manager can add/edit menu items
- UC-10: Manager can generate daily sales report
- Final testing, bug fixing, and UI polish
- Deliverable: Final Product

---

## Slide 9: System Implementation - Key Features

**Customer Application Features**
- Menu browsing with category filtering
- Shopping cart with item customization
- Restaurant branch selection
- Order placement for dine-in or takeaway
- Real-time order status tracking
- Order history viewing

**Kitchen Display System Features**
- Real-time order queue display
- Order ticket visualization
- Status update functionality
- Multi-branch order filtering
- Automatic refresh on new orders

**Point of Sale System Features**
- Table order creation
- Order modification capabilities
- Order search functionality
- Menu management interface
- Staff permission management

---

## Slide 10: System Design - Data Models

**Core Data Models**

**Order Model**
- Order identification and metadata
- Customer and restaurant associations
- Order items with quantities and customizations
- Status tracking (pending, preparing, ready, completed)
- Financial information (subtotal, tax, service charge, total)
- Payment information and timestamps

**MenuItem Model**
- Item identification and details
- Pricing and availability information
- Category classification
- Modifier associations
- Stock management

**User and Admin Models**
- User authentication information
- Role-based permission management
- Restaurant access control
- Profile information

---

## Slide 11: System Design - Database Schema

**Firestore Collections**

**Users Collection**
- User profiles and authentication data
- Customer preferences and order history

**Admins Collection**
- Staff account information
- Role and permission assignments
- Restaurant access permissions

**Orders Collection**
- Complete order information
- Real-time status updates
- Payment and billing details

**MenuItems Collection**
- Menu item catalog
- Pricing and availability
- Category and modifier associations

**Restaurants Collection**
- Branch information
- Configuration settings
- Operational parameters

---

## Slide 12: Software Validation - Testing Strategy

**Testing Approach**

**Acceptance Testing**
- Customer place order successful
- Order appears in kitchen display system
- Status updates propagate correctly
- Payment processing validation

**Unit Testing**
- Price calculation accuracy
- Order total computation
- Data model validation
- Business logic verification

**Performance Testing**
- System response time measurement
- Real-time update latency verification
- Menu browsing performance
- Concurrent user handling

**Integration Testing**
- Cross-application data synchronization
- Firebase service integration
- Real-time listener functionality
- Permission-based access control

---

## Slide 13: Software Validation - Test Results

**Test Case Execution Summary**

**TC-01: User can select restaurant and place order**
- Status: PASS
- Verified restaurant selection functionality
- Confirmed order creation process

**TC-02: Price calculation verification**
- Status: PASS
- Correct price computation with modifiers
- Accurate tax and service charge application

**TC-03: Ordering status update**
- Status: PASS
- Real-time status synchronization verified
- Cross-application update propagation confirmed

**TC-04: Generate order code and details**
- Status: PASS
- Order number generation working correctly
- Complete order details display verified

**TC-05: Member login function**
- Status: PASS
- Member and admin login successful
- Role-based access control functioning

---

## Slide 14: Software Evolution - Current Status

**Completed Features (Sprint 1 & 2)**
- Customer menu browsing and ordering
- Kitchen real-time order display
- Order status tracking
- Table order management
- Order modification capabilities
- Menu management interface
- Role-based access control

**Performance Metrics Achieved**
- Order confirmation: Within 1.0 second (target met)
- Real-time updates: Within 2 seconds (target met)
- Menu browsing: Less than 500ms (target exceeded)

**System Reliability**
- 99.9% uptime maintained during testing
- Data consistency verified across applications
- Real-time synchronization functioning correctly

---

## Slide 15: Software Evolution - Future Enhancements

**Planned Future Features**

**Payment Integration (Future Sprint)**
- UC-2: Customer can pay for online order
- UC-6: Server can process bill with splits and discounts
- Secure payment gateway integration
- Multiple payment method support

**Advanced Management Features**
- Inventory management system
- Low-stock alert functionality
- Advanced reporting and analytics
- Integration with delivery platforms

**System Enhancements**
- Offline mode support
- Push notification system
- Enhanced user experience improvements
- Performance optimizations

---

## Slide 16: Key Achievements

**Technical Achievements**
- Successfully implemented real-time order synchronization
- Achieved performance targets for response times
- Established scalable architecture for multi-branch support
- Implemented comprehensive role-based access control

**Process Achievements**
- Followed Agile/Scrum methodology effectively
- Delivered working increments at end of each sprint
- Maintained code quality and documentation standards
- Achieved stakeholder requirements satisfaction

**Learning Outcomes**
- Applied software engineering principles in practice
- Gained experience with modern Android development
- Understood cloud-based backend architecture
- Implemented real-time data synchronization

---

## Slide 17: Challenges and Solutions

**Technical Challenges**

**Challenge: Real-time Data Synchronization**
- Solution: Implemented Firestore real-time listeners with proper lifecycle management
- Result: Achieved sub-2-second update latency across applications

**Challenge: Multi-branch Permission Management**
- Solution: Developed role-based access control system with restaurant filtering
- Result: Secure and efficient data access control

**Challenge: Performance Optimization**
- Solution: Implemented debouncing, efficient data structures, and query optimization
- Result: Met all performance requirements

**Process Challenges**
- Managing distributed team development
- Balancing feature development with testing
- Maintaining code quality across sprints

---

## Slide 18: System Demonstration Highlights

**Customer Ordering Flow**
- Browse menu by categories
- Select items with customization options
- Review shopping cart
- Place order and receive confirmation
- Track order status in real-time

**Kitchen Display System**
- View incoming orders automatically
- See order details and special instructions
- Update order status with single tap
- Filter orders by restaurant branch

**Server Management**
- Create table orders efficiently
- Modify existing orders
- Search and manage active orders
- Access administrative functions

---

## Slide 19: Conclusion

**Project Summary**
- Successfully developed comprehensive food ordering and tracking system
- Implemented all core user stories from Sprint 1 and Sprint 2
- Achieved performance and reliability targets
- Established foundation for future enhancements

**Key Takeaways**
- Agile methodology enabled iterative development and continuous improvement
- Real-time synchronization critical for multi-application systems
- Role-based access control essential for restaurant operations
- Performance optimization requires careful design and testing

**Future Directions**
- Payment integration and billing features
- Advanced analytics and reporting
- Delivery platform integration
- Enhanced user experience features

---

## Slide 20: Questions and Discussion

**Thank You for Your Attention**

**Group 14 Members:**
- Shing Kit Lui
- On Tai Hung
- Ching Hong Chan
- Holly Lei Stephenson
- Siu Hin Chak
- Kai Ho Wan
- Tsz Fung Wong
- Ching Lo
- Ho Him Cheung
- Ho Ching Yu

**Contact Information**
- Course: COMP 3500 Software Engineering
- Project: GoGoFood - Food Ordering and Tracking System

**We welcome your questions and feedback**




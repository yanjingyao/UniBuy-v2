import requests
import json
import time

BASE_URL = "http://localhost:8080"
SESSION = requests.Session()

def print_result(step, success, msg=""):
    status = "✅ PASS" if success else "❌ FAIL"
    print(f"{status} | {step} | {msg}")

def test_flow():
    print("=== UniBuy Interface Test Start ===\n")

    # 1. Register Student
    student_data = {
        "username": "test_student_" + str(int(time.time())),
        "password": "password123",
        "phone": "1380000" + str(int(time.time())%10000),
        "schoolId": "2024001",
        "nickname": "Test Student"
    }
    try:
        res = requests.post(f"{BASE_URL}/auth/register/student", json=student_data)
        print_result("Register Student", res.status_code == 200, res.json().get('msg'))
    except Exception as e:
        print_result("Register Student", False, str(e))
        return

    # 2. Login Student
    login_data = {
        "username": student_data['username'],
        "password": student_data['password'],
        "role": "student"
    }
    student_id = None
    try:
        res = requests.post(f"{BASE_URL}/auth/login", json=login_data)
        if res.json().get('code') == 200:
            student_id = res.json()['data']['user']['studentId']
            print_result("Login Student", True, f"Student ID: {student_id}")
        else:
            print_result("Login Student", False, res.json().get('msg'))
            return
    except Exception as e:
        print_result("Login Student", False, str(e))
        return

    # 3. Register Merchant
    merchant_data = {
        "username": "test_merchant_" + str(int(time.time())),
        "password": "password123",
        "phone": "1390000" + str(int(time.time())%10000),
        "shopName": "Test Shop",
        "pickupLocation": "Canteen A"
    }
    try:
        res = requests.post(f"{BASE_URL}/auth/register/merchant", json=merchant_data)
        print_result("Register Merchant", res.status_code == 200, res.json().get('msg'))
    except Exception as e:
        print_result("Register Merchant", False, str(e))

    # 4. Login Admin (to audit merchant)
    admin_data = {"username": "admin", "password": "admin123", "role": "admin"}
    try:
        res = requests.post(f"{BASE_URL}/auth/login", json=admin_data)
        print_result("Login Admin", res.json().get('code') == 200, res.json().get('msg'))
        
        # 4.1 Audit Merchant
        # First get pending list
        res = requests.get(f"{BASE_URL}/admin/merchant/pending")
        if res.json().get('code') == 200:
            pending_list = res.json()['data']['records']
            # Find our merchant
            target_merchant = None
            for m in pending_list:
                if m['username'] == merchant_data['username']:
                    target_merchant = m
                    break
            
            if target_merchant:
                m_id = target_merchant['merchantId']
                # Audit pass
                res = requests.post(f"{BASE_URL}/admin/audit/merchant", params={"merchantId": m_id, "status": 1})
                print_result("Audit Merchant", res.json().get('code') == 200, res.json().get('msg'))
            else:
                print_result("Audit Merchant", False, "Merchant not found in pending list")
        else:
            print_result("List Pending Merchants", False, res.json().get('msg'))

    except Exception as e:
        print_result("Admin Operations", False, str(e))
        return

    # 4.2 Login Merchant (Retry after audit)
    merchant_id = None
    try:
        login_data_m = {
            "username": merchant_data['username'],
            "password": merchant_data['password'],
            "role": "merchant"
        }
        res = requests.post(f"{BASE_URL}/auth/login", json=login_data_m)
        if res.json().get('code') == 200:
            merchant_id = res.json()['data']['user']['merchantId']
            print_result("Login Merchant", True, f"Merchant ID: {merchant_id}")
        else:
            print_result("Login Merchant", False, res.json().get('msg'))
    except Exception as e:
        print_result("Login Merchant", False, str(e))

    # 5. Publish Request
    req_data = {
        "studentId": student_id,
        "productName": "PS5 Console",
        "category": "Digital",
        "description": "Need it asap",
        "expectedPrice": 500.00,
        "urgencyLevel": 3, # High
        "minJoinUsers": 1
    }
    request_id = None
    try:
        res = requests.post(f"{BASE_URL}/request/publish", json=req_data)
        print_result("Publish Request", res.json().get('code') == 200, res.json().get('msg'))
        
        # Get the request ID from square
        res = requests.get(f"{BASE_URL}/request/square")
        if res.json().get('code') == 200:
            requests_list = res.json()['data']['records']
            if requests_list:
                request_id = requests_list[0]['requestId']
                print_result("Get Request ID", True, f"ID: {request_id}")
            else:
                print_result("Get Request ID", False, "No requests found")
    except Exception as e:
        print_result("Publish Request", False, str(e))

    # 6. Respond Request (Merchant)
    response_id = None
    if merchant_id and request_id:
        resp_data = {
            "requestId": request_id,
            "merchantId": merchant_id,
            "quotedPrice": 550.00,
            "responseNote": "I can buy it from JD."
        }
        try:
            res = requests.post(f"{BASE_URL}/request/respond", json=resp_data)
            print_result("Respond Request", res.json().get('code') == 200, res.json().get('msg'))
            
            # Get response ID
            res = requests.get(f"{BASE_URL}/request/responses", params={"requestId": request_id})
            if res.json().get('code') == 200:
                responses = res.json()['data']
                if responses:
                    response_id = responses[0]['responseId']
                    print_result("Get Response ID", True, f"ID: {response_id}")
        except Exception as e:
            print_result("Respond Request", False, str(e))

    # 7. Confirm Selection (Student)
    if student_id and request_id and response_id:
        try:
            res = requests.post(f"{BASE_URL}/request/confirm", params={"requestId": request_id, "responseId": response_id})
            print_result("Confirm Selection", res.json().get('code') == 200, res.json().get('msg'))
        except Exception as e:
            print_result("Confirm Selection", False, str(e))

    # 8. List Products (Verify product creation)
    # Note: Product created from request is Pending Audit (0) and Down (0) by default in code logic?
    # Let's check RequestServiceImpl.confirmSelection:
    # product.setAuditStatus(0); product.setStatus(0);
    # So we need Admin to audit it first, OR verify it exists in DB/Merchant List.
    # We'll check Merchant List.
    if merchant_id:
        try:
            res = requests.get(f"{BASE_URL}/product/merchant/list", params={"merchantId": merchant_id})
            if res.json().get('code') == 200:
                products = res.json()['data']['records']
                found = False
                product_id = None
                for p in products:
                    if "PS5" in p['name']:
                        found = True
                        product_id = p['productId']
                        break
                print_result("Verify Product Created", found, f"Product ID: {product_id}" if found else "Not Found")
                
                # 9. Admin Audit Product (so student can see it and order)
                if found:
                     res = requests.post(f"{BASE_URL}/product/audit", params={"productId": product_id, "status": 1})
                     print_result("Audit Product", res.json().get('code') == 200, res.json().get('msg'))
                     
                     # 10. Create Order (Student)
                     if student_id:
                         order_data = {
                             "order": {
                                 "studentId": student_id,
                                 "merchantId": merchant_id,
                                 "totalPrice": 550.00
                             },
                             "items": [
                                 {
                                     "productId": product_id,
                                     "quantity": 1,
                                     "price": 550.00
                                 }
                             ]
                         }
                         res = requests.post(f"{BASE_URL}/order/create", json=order_data)
                         if res.json().get('code') == 200:
                             order_id = res.json()['data']['orderId']
                             print_result("Create Order", True, f"Order ID: {order_id}")
                             
                             # 11. Pay Order (Student)
                             res = requests.post(f"{BASE_URL}/order/pay", params={"orderId": order_id})
                             print_result("Pay Order", res.json().get('code') == 200, res.json().get('msg'))

                             # 12. Accept Order (Merchant)
                             res = requests.post(f"{BASE_URL}/order/accept", params={"orderId": order_id})
                             print_result("Accept Order", res.json().get('code') == 200, res.json().get('msg'))

                             # 13. Upload Voucher (Merchant)
                             res = requests.post(f"{BASE_URL}/order/voucher", params={"orderId": order_id, "url": "http://img.com/voucher.jpg"})
                             print_result("Upload Voucher", res.json().get('code') == 200, res.json().get('msg'))
                             
                             # 14. Finish Order (Student)
                             res = requests.post(f"{BASE_URL}/order/finish", params={"orderId": order_id})
                             print_result("Finish Order", res.json().get('code') == 200, res.json().get('msg'))
                             
                             # 15. Review (Student)
                             review_data = {
                                 "orderId": order_id,
                                 "studentId": student_id,
                                 "merchantId": merchant_id,
                                 "scoreAttitude": 5,
                                 "scoreQuality": 5,
                                 "scoreSpeed": 4,
                                 "comment": "Good service!"
                             }
                             res = requests.post(f"{BASE_URL}/aux/review/submit", json=review_data)
                             print_result("Submit Review", res.json().get('code') == 200, res.json().get('msg'))
                             
                             # 16. Chat (Student)
                             chat_data = {
                                 "orderId": order_id,
                                 "senderId": student_id,
                                 "senderRole": 1,
                                 "receiverId": merchant_id,
                                 "content": "Thank you!",
                                 "type": 0
                             }
                             res = requests.post(f"{BASE_URL}/aux/chat/send", json=chat_data)
                             print_result("Send Chat", res.json().get('code') == 200, res.json().get('msg'))

                         else:
                             print_result("Create Order", False, res.json().get('msg'))
                         
        except Exception as e:
            print_result("Verify Product", False, str(e))

    print("\n=== Test Finished ===")

if __name__ == "__main__":
    test_flow()

package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dto.UserDTO;
import entity.*;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import model.PayHere;

@WebServlet(name = "CheckOut", urlPatterns = {"/CheckOut"})
public class CheckOut extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CheckOut.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject requestJsonObject = gson.fromJson(request.getReader(), JsonObject.class);
        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("success", false);

        Session session = null;
        Transaction transaction = null;

        HttpSession httpSession = request.getSession();
        UserDTO userDTO = (UserDTO) httpSession.getAttribute("user");

        if (userDTO == null) {
            responseJsonObject.addProperty("message", "User not signed in");
            sendJsonResponse(response, gson, responseJsonObject);
            return;
        }

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // Fetch the user from the database
            User user = getUserByEmail(session, userDTO.getEmail());

            if (user == null) {
                responseJsonObject.addProperty("message", "User not found in the database.");
                sendJsonResponse(response, gson, responseJsonObject);
                return;
            }

            // Handle address and checkout process
            boolean isCurrentAddress = requestJsonObject.get("isCurrentAddress").getAsBoolean();
            Address address;

            if (isCurrentAddress) {
                // Get current address
                address = getCurrentAddress(session, user);
                if (address == null) {
                    responseJsonObject.addProperty("message", "Current address not found. Please create a new address.");
                    sendJsonResponse(response, gson, responseJsonObject);
                    return;
                }
            } else {
                // Create new address after validating inputs
                address = createNewAddress(session, requestJsonObject, user, responseJsonObject);
                if (address == null) {
                    sendJsonResponse(response, gson, responseJsonObject);
                    return;
                }
            }

            // Proceed with the checkout process
            List<Cart> cartList = getUserCartItems(session, user);
            if (cartList.isEmpty()) {
                responseJsonObject.addProperty("message", "Your cart is empty.");
                sendJsonResponse(response, gson, responseJsonObject);
                return;
            }

            Order_Status orderStatus = (Order_Status) session.get(Order_Status.class, 5); // Hardcoded for now, can be dynamic
            completeCheckout(session, user, address, cartList, orderStatus);

            transaction.commit();

            responseJsonObject.addProperty("success", true);
            responseJsonObject.addProperty("message", "Checkout successful.");

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error during checkout process", e);
            responseJsonObject.addProperty("message", "An error occurred during checkout.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        sendJsonResponse(response, gson, responseJsonObject);
    }

    // Helper method to send JSON response
    private void sendJsonResponse(HttpServletResponse response, Gson gson, JsonObject responseJsonObject) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseJsonObject));
    }

    // Fetch user by email from the database
    private User getUserByEmail(Session session, String email) {
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("email", email));
        return (User) criteria.uniqueResult();
    }

    // Fetch the user's current address from the database
    private Address getCurrentAddress(Session session, User user) {
        Criteria criteria = session.createCriteria(Address.class);
        criteria.add(Restrictions.eq("user", user));
        criteria.setMaxResults(1);
        return (Address) criteria.uniqueResult();
    }

    // Create a new address after validating the input fields
    private Address createNewAddress(Session session, JsonObject requestJsonObject, User user, JsonObject responseJsonObject) {
        String firstName = requestJsonObject.get("first_name").getAsString();
        String lastName = requestJsonObject.get("last_name").getAsString();
        String cityId = requestJsonObject.get("city_id").getAsString();
        String address1 = requestJsonObject.get("address1").getAsString();
        String address2 = requestJsonObject.get("address2").getAsString();
        String postalCode = requestJsonObject.get("postal_code").getAsString();
        String mobile = requestJsonObject.get("mobile").getAsString();

        if (firstName.isEmpty()) {
            responseJsonObject.addProperty("message", "Please fill in the first name.");
            return null;
        } else if (lastName.isEmpty()) {
            responseJsonObject.addProperty("message", "Please fill in the last name.");
            return null;
        } else if (!Validation.isInteger(cityId)) {
            responseJsonObject.addProperty("message", "Invalid city selected.");
            return null;
        }

        City city = (City) session.get(City.class, Integer.parseInt(cityId));
        if (city == null) {
            responseJsonObject.addProperty("message", "Invalid city selected.");
            return null;
        }

        if (address1.isEmpty()) {
            responseJsonObject.addProperty("message", "Please fill in address line 1.");
            return null;
        } else if (postalCode.isEmpty() || postalCode.length() != 5 || !Validation.isInteger(postalCode)) {
            responseJsonObject.addProperty("message", "Invalid postal code.");
            return null;
        } else if (mobile.isEmpty() || !Validation.isMobileNumberValid(mobile)) {
            responseJsonObject.addProperty("message", "Invalid mobile number.");
            return null;
        }

        Address address = new Address();
        address.setCity(city);
        address.setFirst_name(firstName);
        address.setLast_name(lastName);
        address.setLine1(address1);
        address.setLine2(address2);
        address.setPostal_code(postalCode);
        address.setMobile(mobile);
        address.setUser(user);

        session.save(address);
        return address;
    }

    // Fetch the user's cart items from the database
    private List<Cart> getUserCartItems(Session session, User user) {
        Criteria criteria = session.createCriteria(Cart.class);
        criteria.add(Restrictions.eq("user", user));
        return criteria.list();
    }

    // Complete the checkout process, create orders and order items, and update product quantities
    private void completeCheckout(Session session, User user, Address address, List<Cart> cartList, Order_Status orderStatus) {
        Orders orders = new Orders();
        orders.setAddress_id(address);
        orders.setDate_time(new Date());
        orders.setUser_id(user);

        int order_id = (int) session.save(orders);

        double amount = 0;
        String items = "";
        for (Cart cartItem : cartList) {

            //Calculate amount
            amount += cartItem.getQty() * cartItem.getProduct().getPrice();
            if (address.getCity().getId() == 1) {
                amount += 1000;
            } else {
                amount += 2500;
            }
            //calculate amount

            //get items details
            items += cartItem.getProduct().getTitle() + "x" + cartItem.getQty() + " ";

            Product product = cartItem.getProduct();

            // Validate stock availability
            if (product.getQty() < cartItem.getQty()) {
                throw new RuntimeException("Not enough stock for product");
            }

            // Create order item
            Order_Item orderItem = new Order_Item();
            orderItem.setOrder(orders);
            orderItem.setOrder_Status(orderStatus);
            orderItem.setProduct(product);
            orderItem.setQty(cartItem.getQty());

            session.save(orderItem);

            // Update product stock quantity
            product.setQty(product.getQty() - cartItem.getQty());
            session.update(product);

            // Delete the cart item
              session.delete(cartItem);
          
//            
//            //start payment 
//            String merchant_id = "";
//            String formattedAmount = new DecimalFormat("0.00").format(amount);
//            String currency = "LKR";
//            String merchantSecret = "Mjk1MDA1NzgxMTE2OTY5NTA5NzkyMDgwMDkyMTg5MTkyODE3NjI1OQ==";
//            String merchantSecretMdHash = PayHere.generateMd5(merchantSecret);
//
//            JsonObject payhere = new JsonObject();
//            payhere.addProperty("merchant_id", "1221047");
//
//            payhere.addProperty("return_url", "");
//            payhere.addProperty("cancel_url", "");
//            payhere.addProperty("notify_url", "");
//
//            payhere.addProperty("first_name", user.getFirst_name());
//            payhere.addProperty("last_name", user.getLast_name());
//            payhere.addProperty("email", user.getEmail());
//            payhere.addProperty("phone", "");
//            payhere.addProperty("address", "");
//            payhere.addProperty("city", "");
//            payhere.addProperty("country", "");
//            payhere.addProperty("order_id", String.valueOf(order_id));
//            payhere.addProperty("items", "");
//            payhere.addProperty("currency", currency);
//            payhere.addProperty("amount", formattedAmount);
//            payhere.addProperty("sandbox", true);
//
//            //Generate MD5 Hash
//            String md5Hash = PayHere.generateMd5(merchant_id + order_id + formattedAmount + currency + merchantSecretMdHash);
//            payhere.addProperty("hash", md5Hash);
//
//            //End set payment
//            JsonObject responseJsonObject = new JsonObject();
//            responseJsonObject.addProperty("success", true);
//            responseJsonObject.addProperty("message", "Checkout Completed");

//            Gson gson = new Gson();
//            responseJsonObject.add("payhereJson", gson.toJsonTree(payhere));
        }
    }
}
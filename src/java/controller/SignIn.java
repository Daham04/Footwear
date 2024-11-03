package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Cart_DTO;
import dto.Response_DTO;
import dto.UserDTO;
import entity.Cart;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Response_DTO response_dto = new Response_DTO();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        UserDTO user_dto = gson.fromJson(request.getReader(), UserDTO.class);

        if (user_dto.getEmail().isEmpty()) {
            response_dto.setContent("Please Enter Your Email");
        } else if (user_dto.getPassword().isEmpty()) {
            response_dto.setContent("Please Enter Your Password");
        } else {

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(User.class);
            criteria1.add(Restrictions.eq("email", user_dto.getEmail()));
            criteria1.add(Restrictions.eq("password", user_dto.getPassword()));

            if (!criteria1.list().isEmpty()) {

                User user = (User) criteria1.list().get(0);

                if (!user.getVerification().equals("Verified")) {
                    //not verified

                    request.getSession().setAttribute("email", user_dto.getEmail());
                    response_dto.setContent("Unverified");

                } else {
                    //verified
                    user_dto.setFirst_name(user.getFirst_name());
                    user_dto.setLast_name(user.getLast_name());
                    user_dto.setPassword(null);
                    request.getSession().setAttribute("user", user_dto);

                    //Transfer Session Cart to DB Cart
                    if (request.getSession().getAttribute("sessionCart") != null) {
                        //session cart found

                        ArrayList<Cart_DTO> sessionCart = (ArrayList<Cart_DTO>) request.getSession().getAttribute("sessionCart");

                        Criteria criteria2 = session.createCriteria(Cart.class);
                        criteria2.add(Restrictions.eq("user", user));
                        List<Cart> dbCart = criteria2.list();

                        if (dbCart.isEmpty()) {
                            //DB cart empty
                            // Add all session items into DB cart

                            for (Cart_DTO cart_DTO : sessionCart) {

                                Cart cart = new Cart();
                                cart.setProduct(cart_DTO.getProduct()); //*
                                cart.setQty(cart_DTO.getQty());
                                cart.setUser(user);

                                session.save(cart);

                            }

                        } else {
                            //Found items in db cart

                            for (Cart_DTO cart_DTO : sessionCart) {

                                boolean isFoundInDbCart = false;

                                for (Cart cart : dbCart) {

                                    if (cart_DTO.getProduct().getId() == cart.getProduct().getId()) {
                                        //same product found
                                        isFoundInDbCart = true;
                                        
                                        if((cart_DTO.getQty() + cart.getQty()) <= cart.getProduct().getQty()){
                                           //quantity available
                                           cart.setQty(cart_DTO.getQty() + cart.getQty());
                                           session.update(cart);
                                           
                                        }else{
                                        //qunatity not available
                                            cart.setQty(cart.getProduct().getQty());
                                            session.update(cart);
                                        }
                                    }

                                }

                                if (!isFoundInDbCart) {
                                    // not found in db cart

                                    Cart cart = new Cart();
                                    cart.setProduct(cart_DTO.getProduct()); //*
                                    cart.setQty(cart_DTO.getQty());
                                    cart.setUser(user);

                                    session.save(cart);
                                }
                            }

                        }

                        request.getSession().removeAttribute("sessionCart");
                        session.beginTransaction().commit();

                    }

                    response_dto.setSuccess(true);
                    response_dto.setContent("Sign In Success");
                }

            } else {
                response_dto.setContent("Invalid Details! Please try again");

            }

        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(response_dto));
    }

}

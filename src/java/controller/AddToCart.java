package controller;

import com.google.gson.Gson;
import dto.Cart_DTO;
import dto.Response_DTO;
import dto.UserDTO;
import entity.Cart;
import entity.Product;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.HibernateUtil;
import model.Validation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Response_DTO response_DTO = new Response_DTO();

        Gson gson = new Gson();

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {

            String id = request.getParameter("id");
            String qty = request.getParameter("qty");

            if (!Validation.isInteger(id)) {
                //Produt Not Found
                response_DTO.setContent("Product not found");

            } else if (!Validation.isInteger(qty)) {
                //Invalid Quantity
                response_DTO.setContent("Invalid quantity");

            } else {

                int productId = Integer.parseInt(id);
                int productQty = Integer.parseInt(qty);

                if (productQty <= 0) {
                    //Quantity must be greater than 0
                    response_DTO.setContent("Quantity must be greater than 0");

                } else {

                    Product product = (Product) session.get(Product.class, productId);

                    if (product != null) {
                        //Product found

                        if (request.getSession().getAttribute("user") != null) {
                            //DB Cart

                            UserDTO userDto = (UserDTO) request.getSession().getAttribute("user");

                            //get db User
                            Criteria criteria1 = session.createCriteria(User.class);
                            criteria1.add(Restrictions.eq("email", userDto.getEmail()));
                            User user = (User) criteria1.uniqueResult();

                            //check in db cart
                            Criteria criteria2 = session.createCriteria(Cart.class);
                            criteria2.add(Restrictions.eq("user", user));
                            criteria2.add(Restrictions.eq("product", product));

                            if (criteria2.list().isEmpty()) {
                                //item not found in cart

                                if (productQty <= product.getQty()) {
                                    //Add product to cart
                                    Cart cart = new Cart();
                                    cart.setProduct(product);
                                    cart.setQty(productQty);
                                    cart.setUser(user);
                                    session.save(cart);

                                    product.setQty(product.getQty() - productQty);
                                    session.save(product);
                                    transaction.commit();

                                    response_DTO.setSuccess(true);
                                    response_DTO.setContent("Product added to the cart");

                                } else {
                                    //quantity no availabale
                                    response_DTO.setContent("quantity no availabale");
                                }

                            } else {
                                //Item already found in cart

                                Cart cartitem = (Cart) criteria2.uniqueResult();

                                if ((cartitem.getQty() + productQty) <= product.getQty()) {

                                    cartitem.setQty(cartitem.getQty() + productQty);
                                    session.save(cartitem);

                                    product.setQty(product.getQty() - productQty);
                                    session.save(product);
                                    transaction.commit();

                                    response_DTO.setSuccess(true);
                                    response_DTO.setContent("Cart item updated");

                                } else {
                                    //Can't update your cart.Quantity not available
                                    response_DTO.setContent("Can't update your cart.Quantity not available");

                                }

                            }

                        } else {
                            //Session Cart

                            HttpSession httpSession = request.getSession();
                            if (httpSession.getAttribute("sessionCart") != null) {
                                //session cart found

                                ArrayList<Cart_DTO> sessionCart = (ArrayList<Cart_DTO>) httpSession.getAttribute("sessionCart");

                                Cart_DTO foundcart_dto = null;

                                for (Cart_DTO cart_DTO : sessionCart) {
                                    if (cart_DTO.getProduct().getId() == product.getId()) {
                                        foundcart_dto = cart_DTO;
                                        break;
                                    }
                                }

                                if (foundcart_dto != null) {
                                    //Prouct found

                                    if ((foundcart_dto.getQty() + productQty) <= product.getQty()) {
                                        //Update quantity
                                        foundcart_dto.setQty(foundcart_dto.getQty() + productQty);

                                    } else {
                                        //quantity not available
                                        response_DTO.setContent("quantity not available");
                                    }

                                } else {
                                    //product not found

                                    if (productQty <= product.getQty()) {
                                        //Add to session cart
                                        Cart_DTO cart_dto = new Cart_DTO();
                                        cart_dto.setProduct(product);
                                        cart_dto.setQty(productQty);
                                        sessionCart.add(cart_dto);

                                        response_DTO.setSuccess(true);
                                        response_DTO.setContent("Cart item updated");

                                    } else {
                                        //Quantity not available
                                        response_DTO.setContent("Quantity not available");
                                    }

                                }

                            } else {
                                //session cart not found

                                if (productQty <= product.getQty()) {
                                    //add to session cart

                                    ArrayList<Cart_DTO> sessionCart = new ArrayList<>();

                                    Cart_DTO cart_dto = new Cart_DTO();
                                    cart_dto.setProduct(product);
                                    cart_dto.setQty(productQty);
                                    sessionCart.add(cart_dto);
                                    
                                    product.setQty(product.getQty() - productQty);
                                    session.save(product);

                                    request.getSession().setAttribute("sessionCart", sessionCart);

                                    response_DTO.setSuccess(true);
                                    response_DTO.setContent("Product added to the cart");

                                } else {
                                    //Quantity not available
                                    response_DTO.setContent("Quantity not available");
                                }

                            }

                        }

                    } else {
                        //Product not Found 
                        response_DTO.setContent("Product not Found");
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            response_DTO.setContent("Product not Found");

        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(response_DTO));
        session.close();

    }

}

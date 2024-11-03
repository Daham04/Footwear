package controller;

import com.google.gson.Gson;
import dto.Cart_DTO;
import dto.UserDTO;
import entity.Cart;
import entity.Product;
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
import javax.servlet.http.HttpSession;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadCartItems", urlPatterns = {"/LoadCartItems"})
public class LoadCartItems extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
       
        HttpSession httpSession = request.getSession();
        
        Session session = HibernateUtil.getSessionFactory().openSession();

        ArrayList<Cart_DTO> cart_dto_list = new ArrayList<>();
        
        try {
            if (httpSession.getAttribute("user") != null) {
                // DB Cart
                UserDTO user_dto = (UserDTO) request.getSession().getAttribute("user");

                Criteria criteria1 = session.createCriteria(User.class);
                criteria1.add(Restrictions.eq("email", user_dto.getEmail()));
                User user = (User) criteria1.uniqueResult();

                Criteria criteria2 = session.createCriteria(Cart.class);
                criteria2.add(Restrictions.eq("user", user));

                List<Cart> cartList = criteria2.list();

                for (Cart cart : cartList) {
                    
                    Cart_DTO cart_dto = new Cart_DTO();
                    
                    Product product = cart.getProduct();
                    product.setUser(null);
                    cart_dto.setProduct(product);
                    
                    cart_dto.setQty(cart.getQty());
                    
                    cart_dto_list.add(cart_dto);
                }

            } else {
                // Session Cart
                if (httpSession.getAttribute("sessionCart") != null) {

                    cart_dto_list = (ArrayList<Cart_DTO>) httpSession.getAttribute("sessionCart");
                    
                    for (Cart_DTO cart_DTO : cart_dto_list) {
                        cart_DTO.getProduct().setUser(null);
                        
                    }

                } else {
                    //cart empty
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(cart_dto_list));
        session.close();

    }
}

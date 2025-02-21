package ostro.veda.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import ostro.veda.common.dto.OrderDTO;
import ostro.veda.common.dto.OrderDetailDTO;
import ostro.veda.common.dto.OrderStatusHistoryDTO;
import ostro.veda.common.dto.ProductDTO;
import ostro.veda.db.helpers.JPAUtil;
import ostro.veda.db.jpa.Address;
import ostro.veda.db.jpa.Order;
import ostro.veda.loggerService.Logger;

import java.util.List;
import java.util.Map;

public class OrderRepository extends Repository {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderRepository(EntityManager em, OrderDetailRepository orderDetailRepository, OrderStatusHistoryRepository orderStatusHistoryRepository) {
        super(em);
        this.orderDetailRepository = orderDetailRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    /**
     *
     * @param userId validated at OrderService
     * @param totalAmount |
     * @param status |
     * @param shippingAddress |
     * @param billingAddress |
     * @param productAndQuantity validated at OrderService
     * @return returns the persisted OrderDTO
     */
    public OrderDTO addOrder(int userId, double totalAmount, String status, Address shippingAddress,
                             Address billingAddress, Map<ProductDTO, Integer> productAndQuantity) {

        Order order = getNewOrder(userId, totalAmount, status, shippingAddress, billingAddress);
        OrderDTO orderDTO = null;

        EntityTransaction transaction = null;
        try {
            transaction = this.em.getTransaction();
            transaction.begin();

            this.em.persist(order);
            List<OrderDetailDTO> orderDetailDTOList = orderDetailRepository.addOrder(productAndQuantity, order);
            OrderStatusHistoryDTO orderStatusHistoryDTO = orderStatusHistoryRepository.addOrder(order, status);

            transaction.commit();
            orderDTO = order.transformToDto();
            orderDTO.getOrderDetails().addAll(orderDetailDTOList);
            orderDTO.getOrderStatusHistory().add(orderStatusHistoryDTO);
        } catch (Exception e) {
            Logger.log(e);
            JPAUtil.transactionRollBack(transaction);
        }

        return orderDTO;
    }

    /**
     *
     * @param userId required field
     * @param totalAmount required field
     * @param status required field
     * @param shippingAddress required field
     * @param billingAddress required field
     * @return returns Order DAO to be persisted
     */
    private static Order getNewOrder(int userId, double totalAmount, String status, Address shippingAddress, Address billingAddress) {
        return new Order(userId, totalAmount, status, shippingAddress, billingAddress, null);
    }

    /**
     * Called when an Order has it's Status updated (e.g. PENDING -> IN_TRANSIT)
     * Used to create an Order Status History of the Order
     * @param orderId validated at OrderService
     * @param newStatus |
     * @return returns the persisted OrderDTO
     */
    public OrderDTO updateOrderStatus(int orderId, String newStatus) {
        Order order = getOrder(orderId);
        // If no order is found, although a valid ID (id > 0), no matching order was found
        if (order == null) return null;
        // Updates Order DAO with new Status to be persisted
        order.updateOrderStatus(newStatus);
        EntityTransaction transaction = null;
        try {
            transaction = this.em.getTransaction();
            transaction.begin();

            this.em.persist(order);
            OrderStatusHistoryDTO orderStatusHistoryDTO = orderStatusHistoryRepository.addOrder(order, newStatus);

            transaction.commit();
            OrderDTO orderDTO = order.transformToDto();
            orderDTO.getOrderStatusHistory().add(orderStatusHistoryDTO);
            return orderDTO;
        } catch (Exception e) {
            Logger.log(e);
            JPAUtil.transactionRollBack(transaction);
        }
        return null;
    }

    /**
     * Gets the Order DAO entity
     * @param orderId Validated at OrderService
     * @return returns Order DAO to be persisted
     */
    private Order getOrder(int orderId) {
        return this.getEm().find(Order.class, orderId);
    }
}

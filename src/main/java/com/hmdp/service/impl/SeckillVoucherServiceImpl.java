package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher>
        implements ISeckillVoucherService {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
//    @Transactional // 先释放锁再提交事务，反而导致另一个线程获取脏数据
    public synchronized Result seckillVoucher(Long voucherId) {

        // 1. 校验时间，库存等信息
        SeckillVoucher voucher = query().eq("voucher_id", voucherId).one();
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime beginTime = voucher.getBeginTime();
        if (currentTime.isBefore(beginTime)) {
            return Result.fail("not begin"); // 秒杀活动尚未开始
        }
        LocalDateTime endTime = voucher.getEndTime();
        if (currentTime.isAfter(endTime)) {
            return Result.fail("already end"); // 秒杀活动已经结束
        }
        if (voucher.getStock() <= 0) {
            return Result.fail("no stock"); // 库存不足
        }

        Long orderId;
//        Long userId = UserHolder.getUser().getId();
//        String strUserId = userId.toString().intern();
//        synchronized (strUserId) {
//            System.out.println("System.identityHashCode(strUserId): " + System.identityHashCode(strUserId));
//            ISeckillVoucherService service = (ISeckillVoucherService)AopContext.currentProxy();
            // 先提交事务，再释放锁
            // 有锁了 其实可以没必要使用事务了
//            orderId = service.createSeckillVoucher(voucherId);
        orderId = createSeckillVoucher(voucherId);
//        }
        return Result.ok(orderId);
    }

//    @Transactional
    public Long createSeckillVoucher(Long voucherId) {
        // 限制一个用户一张券只能购买一次
        int i = voucherOrderService.query().eq("voucher_id", voucherId)
                .eq("user_id", UserHolder.getUser().getId()).count();
        if (i > 0) {
            throw new RuntimeException("one order per user"); // 购买受限，你已买过该券。一种券一个用户限购一张
        }

        // 3. 减卖家库存
        LocalDateTime now = LocalDateTime.now();
        boolean b1 = update().setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).gt("stock", 0)
                .lt("begin_time", now).gt("end_time", now).update();
        if (!b1) {
            throw new RuntimeException("stock-1 error"); // 扣减库存失败
        }

        // 5. 创建交易单，状态支付成功
        Long orderId = redisIdWorker.nextId("orderId");
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(UserHolder.getUser().getId());
        order.setVoucherId(voucherId);
        order.setPayTime(LocalDateTime.now());
        order.setPayType(1); // 支付方式 1：余额支付；2：支付宝；3：微信
        order.setStatus(2); // 订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款
        boolean b2 = voucherOrderService.save(order);
        if (!b2) {
            throw new RuntimeException("create order error"); // 创建订单失败
        }

        return orderId;
    }
}

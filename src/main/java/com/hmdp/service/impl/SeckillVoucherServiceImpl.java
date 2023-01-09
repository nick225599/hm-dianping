package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.User;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.apache.ibatis.transaction.Transaction;
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
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
    public Result seckillVoucher(Long voucherId) {

        // 1. 校验时间，库存等信息
        SeckillVoucher voucher = query().eq("voucher_id", voucherId).one();

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime beginTime = voucher.getBeginTime();
        if(currentTime.isBefore(beginTime)){
            return Result.fail("秒杀活动尚未开始");
        }
        LocalDateTime endTime = voucher.getEndTime();
        if(currentTime.isAfter(endTime)){
            return Result.fail("秒杀活动已经结束");
        }
        if(voucher.getStock() <= 0){
            return Result.fail("库存不足");
        }

        // 2. 事务开始 暂时不考虑

        // 3. 减卖家库存
        voucher.setStock(voucher.getStock() - 1);
        boolean b1 = update().eq("voucher_id", voucherId).
                eq("stock", voucher.getStock())
                .update(voucher);
        if(!b1){
            return Result.fail("扣减库存失败");

        }
        // 4. 增买家资产 暂时不考虑
        // 5. 创建交易单，状态支付成功
        //TODO scs 20230109 单号自增会有什么问题？
        Long orderId = redisIdWorker.nextId("orderId");
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(UserHolder.getUser().getId()) ;
        order.setVoucherId(voucherId);
        order.setPayType(1);
        boolean b2 = voucherOrderService.save(order);
        if(!b2){
            return Result.fail("创建订单失败");
        }

        // 6. 事务完成
        return Result.ok(orderId);
    }
}

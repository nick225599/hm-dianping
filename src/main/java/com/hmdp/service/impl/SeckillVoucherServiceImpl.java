package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.User;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

        // 2. 事务开始

        // 3. 减卖家库存
        // 4. 增买家资产
        // 5. 创建交易单，状态支付成功
        // insert into tb_voucher_order

        // 6. 事务完成
        
        //        return Result.fail("功能未完成");
        return Result.ok();
    }
}

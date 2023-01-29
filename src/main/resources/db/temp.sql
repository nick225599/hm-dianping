-- 将库存改回 100 张
update tb_seckill_voucher set stock = 100 where voucher_id = 10;

-- 秒杀券的库存
select * from tb_seckill_voucher tsv where 1=1;

-- 清空秒杀券的订单
delete from tb_voucher_order tvo where 1=1;

-- 查看秒杀券的订单情况
select * from tb_voucher_order tvo where 1=1;


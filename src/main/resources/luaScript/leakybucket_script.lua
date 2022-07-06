
-- 接口限流
-- last_leaking_time 最后访问时间的毫秒
-- remaining_capacity 当前令牌桶中可用请求令牌数
-- capacity 令牌桶容量
-- leaking_rate    令牌桶添加令牌的速率

-- 获取令牌桶的配置信息
local rate_limit_info = redis.call("HGETALL", KEYS[1])

-- 获取当前时间戳
local timestamp = redis.call("TIME")
local now = math.floor((timestamp[1] * 1000000 + timestamp[2]) / 1000)

if rate_limit_info == nil then -- 没有设置限流配置,则默认拿到令牌
    return now * 10 + 1
end

local last_leaking_time = tonumber(rate_limit_info[2])
local leaking_rate = tonumber(rate_limit_info[4])
local remaining_capacity = tonumber(rate_limit_info[6])
local capacity = tonumber(rate_limit_info[8])

-- 计算需要补给的令牌数,更新令牌数和补给时间戳
local supply_token = math.floor((now - last_leaking_time) / 1000) * leaking_rate
if (supply_token > 0) then
   last_leaking_time = now
   remaining_capacity = supply_token + remaining_capacity
   if remaining_capacity > capacity then
      remaining_capacity = capacity
   end
end

local result = 0 -- 返回结果是否能够拿到令牌,默认否

-- 计算请求是否能够拿到令牌
if (remaining_capacity > 0) then
    remaining_capacity = remaining_capacity - 1
    result = 1
end

-- 更新令牌桶的配置信息
redis.call("HMSET", KEYS[1], "remainingCapacity", remaining_capacity, "lastLeakingTime", last_leaking_time)

-- return now * 10 + result
return result
local key = "counter_limiter:" .. KEYS[1]
local limit = tonumber(ARGV[1])
local expire_time = ARGV[2]

local is_exists = redis.call("EXISTS", key)
if is_exists == 1 then
    if redis.call("GET", key) + 1 > limit then
        return false
    else
        redis.call("INCR", key)
        return true
    end
else
    redis.call("SET", key, 1)
    redis.call("EXPIRE", key, expire_time)
    return true
end

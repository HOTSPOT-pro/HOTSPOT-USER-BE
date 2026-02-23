-- usage-sum.lua
-- Redis ZSET score는 KB 단위로 저장되어 있음
-- KEYS: 여러 usage key
-- RETURN: 각 key의 score 총합 (KB)

local result = {}

for i = 1, #KEYS do
	local sum = 0
	local values = redis.call('ZRANGE', KEYS[i], 0, -1, 'WITHSCORES')

	for j = 2, #values, 2 do
		sum = sum + tonumber(values[j])
	end

	result[i] = sum
end

return result
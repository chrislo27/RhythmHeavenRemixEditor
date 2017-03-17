-- RHRE2 games used outputter
-- Prints out a list of all the games you used.

print "Outputting games used\n"

do
    local out = ""
	local seriesUsed = {}

    for k, v in pairs(remix.gamesUsed) do
		local game = registry.games[v]
        if game == nil then
            out = out .. ("Nil game value? " .. tostring(v) .. ", ")
        else
            out = out .. (tostring(game.name) .. ", ")
			if seriesUsed[game.series] == nil then
				seriesUsed[game.series] = 0
			end
			seriesUsed[game.series] = tonumber(seriesUsed[game.series]) + 1
        end
    end

    print(out)
	
	do
		print "\nGames from each series used:"
		local seriesOut = ""
		for k, v in pairs(seriesUsed) do
			seriesOut = seriesOut .. k .. ": " .. tostring(v) .. "\n"
		end
		
		print(seriesOut)
	end
end

print "\nEND OF OUTPUT"
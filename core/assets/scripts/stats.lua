-- RHRE2 games used outputter
-- Prints out a list of all the games you used.

print "Outputting games used"
print()

do
    local out = ""

    for k, v in pairs(remix.gamesUsed) do
		local game = registry.games[v]
        if game == nil then
            out = out .. ("Nil game value? " .. tostring(v) .. ", ")
        else
            out = out .. (tostring(game.name) .. ", ")
        end
    end

    print(out)
end

print()

print "END OF OUTPUT"
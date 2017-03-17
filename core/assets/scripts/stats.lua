-- RHRE2 games used outputter
-- Prints out a list of all the games you used.

print "Outputting games used"
print()

do
    local list = {}

    for k, v in pairs(remix.entities) do
        local game = v.gameID
        if (game ~= nil and list[game] == nil) then
            list[game] = game
        end
    end

    local out = ""

    for k, v in pairs(list) do
        if registry.games[v] == nil then
            out = out .. ("Nil game value? " .. tostring(v) .. ", ")
        else
            out = out .. (tostring(registry.games[v].name) .. ", ")
        end
    end

    print(out)
end

print()

print "END OF OUTPUT"
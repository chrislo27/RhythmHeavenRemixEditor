-- RHRE2 stats outputter

print "Outputting stats"
print()

print("Playback: " .. tostring(remix.playbackStart))
print("Music: " .. tostring(remix.musicStart) .. " volume " .. tostring(tonumber(remix.musicVolume) * 100) .. "%")
print("Duration: " .. tostring(remix.duration))
print("Cues:")
for k, v in pairs(remix.entities) do
    print("Cue " .. tostring(k) .. " is [" .. tostring(v.beat) .. ", " .. tostring(v.duration) .. ", " .. tostring(v.track) .. ", " .. tostring(v.id) .. ", " .. tostring(v.isPattern) .. ", " .. tostring(v.semitone) .. "]")
end

print()
print("Tempo changes:")
for k, v in pairs(remix.tempoChanges) do
    print("Tempo change " .. tostring(k) .. " at " .. tostring(v.beat) .. " (" .. tostring(v.seconds) .. " s), to " .. tostring(v.tempo) .. " BPM")
end

print()
print("Used games:")
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

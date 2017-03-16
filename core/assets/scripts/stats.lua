-- RHRE2 stats outputter

print "Outputting stats"
print()

print("Playback: " .. tostring(remix.playbackStart))
print("Music: " .. tostring(remix.musicStart) .. " volume " .. tostring(remix.musicVolume) .. "%")
print("Duration: " .. tostring(remix.duration))
print("Cues:")
for k, v in pairs(remix.cues) do
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

end

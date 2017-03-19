-- midi scale

remix:removeAllCues()

local beat = 0.0

local function addNote(semitone, length) 
	local index = remix:addCue("gleeClub/singLoop", beat, 1, length)
	remix:changeCueSemitone(index, semitone)
	remix:addCue("gleeClub/singEnd", beat + length, 1, 1.0)
	beat = beat + length
end

local function parseTable(tbl)
	for k, v in pairs(tbl) do 
		if type(v) == "string" then
			beat = beat + tonumber(v)
		else
			addNote(v, 1.0)
		end
	end
end

do
	local cMajor = {0, 2, 4, 5, 7, 9, 11, 12}
	
	parseTable(cMajor)
end


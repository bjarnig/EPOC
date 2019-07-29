
BScoreItem
{
	var <>item, <>start, <>duration, <>movements;
	
	*new { |item, start, duration, movements|
		^super.newCopyArgs(item, start, duration, movements);
	}
}

BScore
{
	var <>items, <>start;
	
	*new{|items, start|
		^super.newCopyArgs(nil, start).init(items);
	}
	
	init{|itemsIn|
		items = List.new();
		itemsIn.do({arg item; items.add(item)});
	}
	
	play{
		var events, simpleScore;
		
		events = List.new();
		
		items.do({arg scoreItem; 
		
			events.add([scoreItem.start, scoreItem.duration, scoreItem.item.playFunction]);

			scoreItem.movements.do ({arg mov; 
		
				events.add([scoreItem.start + mov.start, mov.getDuration.value, mov.simpleMovement(scoreItem.item)]);
		
			});
		});
	
		simpleScore = SimpleScore2(events);
		simpleScore.play(start);
	}
}
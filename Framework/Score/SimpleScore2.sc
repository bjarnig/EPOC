SimpleScore2{

	var <events;
	var <clock;

	*new{ |events|
		^super.newCopyArgs(events)
	}

	play{ |atTime=0|

		clock = TempoClock.new;
		this.events.do{ |array|
			var start, dur, func;
			#start, dur, func = array;
			[start,dur,func].postln;
			if((start+dur) > atTime){
				clock.sched(if(start<=atTime){0}{start-atTime}, { func.((atTime-start).max(0)); nil });
			};

		}
	}

	stop{ clock.clear }
}

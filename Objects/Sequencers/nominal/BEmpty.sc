
BEmpty : BSeq
{
	var isPlaying; 
	
	*new { |id=0, description, duration, control, outBus=0|
		
		^super.newCopyArgs(id, description, duration, control, outBus).init(); 
	}
	
	init {
		
		this.setDescription;
		isPlaying = 0;
	}
	
	*loadSynthDefs {
	}	
	
	update {	
	}
	
	play {
		this.update.value;
	}
	
	playDuration 
	{	
		Routine 
		{
			1.do({	
			duration.wait;
			isPlaying = 0;
		})
		}.play;
	}

	stop {
		
		Routine 
		{
			1.do({	
			isPlaying = 0;
		})
		}.play;
	}
	
	setDescription {
		description = "";
	}
}
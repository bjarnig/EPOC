
BEgoro : Beffect
{
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}

	init {|values|
		wrap = Bwrap.new(\goro, values);
		this.setDescription;
	}

	*loadSynthDefs {


# EPOC
A SuperCollider framework that addresses interaction and relationships between materials and processes, and their influence on development and form in music. The main concern is how to define musical processes as operational objects, how these objects can interact in a network of objects and how this can create a flexible and adaptable composition environment.

![alt text](https://bjarnig.s3.eu-central-1.amazonaws.com/images/epoc-git.png)

```javascript

/* Install EPOC */

Quarks.install("https://github.com/bjarnig/EPOC")


/* Init some objects */

BUtils.loadLiveObjects;

(
     /* Add the 8 objects */

	~items = Array.new(8);
	~items.add(BNetworkItem(BLSynth5, [], "First"));
	~items.add(BNetworkItem(BLGest8, [\sound, BUtils.materialDir ++ "micralis.wav"], "Second"));
	~items.add(BNetworkItem(BLGest8, [\sound, BUtils.materialDir ++ "texture.wav"], "Third"));
	~items.add(BNetworkItem(BLSynth3, [], "Fourth"));
	~items.add(BNetworkItem(BLSynth7, [], "Fifth"));
	~items.add(BNetworkItem(BLGest4, [\sound, BUtils.materialDir ++ "sequence.wav"], "Sixth"));
	~items.add(BNetworkItem(BLPat6, [\frequencies, [ 8000, 8300, 8600, 8900 ], \waveform, 2], "Seventh"));
	~items.add(BNetworkItem(BLPat1, [\sounds, BUtils.materialDir ++ "grains/*"], "Eighth"));
	~items.add(BNetworkItem.new(BLSynth2, [], "Nine"));
)

/* Load an editor for the objects */

EpocEditor.view(~items, List());

```

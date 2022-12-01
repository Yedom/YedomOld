// MediaElementJS video player with full screen icon
$('video').mediaelementplayer({
    alwaysShowControls: true,
    features: ['playpause','progress','current','duration','tracks','volume','fullscreen'],
    videoVolume: 'horizontal',
    audioWidth: 400,
    audioHeight: 30,
    startVolume: 0.8,
    toggleCaptionsButtonWhenOnlyOne: true,
    iconSprite: iconPath
});
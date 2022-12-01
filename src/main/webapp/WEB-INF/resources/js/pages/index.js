let image = $('#image');

function isTouchDevice() {
    return (('ontouchstart' in window) ||
        (navigator.maxTouchPoints > 0) ||
        (navigator.msMaxTouchPoints > 0));
}

image.attr('hidden', 'hidden');
let container = document.getElementById('model-container');

let scene = new THREE.Scene();
let camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.5, 1000);

let renderer = new THREE.WebGLRenderer();
renderer.setPixelRatio(window.devicePixelRatio);
renderer.setClearColor(0xeeeeee);
renderer.setSize(innerWidth, innerHeight);
renderer.domElement.setAttribute("id", "model-3d");
container.appendChild(renderer.domElement);

let controls = new THREE.OrbitControls(camera, renderer.domElement);
let x = -15;
let y = 50;
let z = -15;
if($(window).width() < 800) {
    x = -35;
    z = -20;
    y = 60;
}
camera.position.set(x, y, z);
controls.target.set(40, 30, 5);
controls.enabled = false;
controls.update();

const aLight = new THREE.DirectionalLight(0xffffff, 1.5);
aLight.position.set(50, 60, -40);
scene.add(aLight, new THREE.AmbientLight(0xffffff, 0.8));

let loader = new THREE.GLTFLoader();

var mixer, clock;
clock = new THREE.Clock();
loader.load('../resources/models/yedom/yedom.gltf', function (gltf) {
    scene.add(gltf.scene);

    mixer = new THREE.AnimationMixer(gltf.scene);
    gltf.animations.forEach((clip) => {
        mixer.clipAction(clip).play();
    } );
    $('#logo').removeAttr('hidden');
});

renderer.setAnimationLoop(_ => {
    if (mixer) mixer.update(clock.getDelta());
    renderer.render(scene, camera);
});

container.addEventListener('mousedown', function (event) {
    event.preventDefault();
}, false);

let oldX = 0;
let oldY = 0;

if(!isTouchDevice()) {
    window.onmousemove = function (e) {
        let changeX = e.x - oldX;
        let changeY = e.y - oldY;

        camera.position.z += changeX / 200;
        camera.position.y -= changeY / 1000;

        controls.target.x += changeX / 200;
        controls.target.z -= changeX / 150;
        controls.target.y -= changeY / 1000;
        controls.update();

        oldX = e.x;
        oldY = e.y;
    }
}

window.onload = function () {
    let particles = $('.particles-js-canvas-el');
    let top = $('footer').offset().top - 50;
    particles.attr('style', 'height: ' + top + 'px !important');
    particles.attr('height', top);

    pJSDom[0].pJS.fn.particlesRefresh();
}
var gulp = require('gulp');
var sass = require('gulp-sass');
var prefix = require('gulp-autoprefixer');
var browserSync = require('browser-sync');

var src = {
	scss: 'app/assets/*.scss',
	css: 'app/dist',
	js: 'app/public/js',
	html: 'app/views/*.html'
};

gulp.task('sass', function () {
	return gulp.src(src.scss)
	.pipe(sass({outputStyle: 'compressed', sourceComments: 'map'}, {errLogToConsole: true}))
	.pipe(prefix("last 2 versions", "> 1%", "ie 8", "Android 2", "Firefox ESR"))
	.pipe(gulp.dest(src.css));
});

gulp.task('bs', function () {
	browserSync({
		// Play Port
		proxy: 'localhost:9000',
		// BrowserSync Port
		port: 9001,
		// Watched files
		files: [src.css, src.js, src.html],
		// Open browser on activator run
		open: false
	});
});

gulp.task('default', ['sass', 'bs']);

gulp.task('clean', function () {
});
//If other sbt tasks are missing, check https://github.com/mmizutani/sbt-play-gulp#how-this-works
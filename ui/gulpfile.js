'use strict';

var gulp					=	require('gulp');
var sass					=	require('gulp-sass');
var prefix				=	require('gulp-autoprefixer');
var browserSync 	=	require('browser-sync');

var reload				=	browserSync.reload;

var src = {
	scss:						'../app/assets/**/*.scss',
	js:							'../public/js/',
	scala:					'../app/views/*.scala.html'
};

var dest = {
	css:						'../target/web/public/main',
	html: 					'../target/scala-2.11/twirl/main/views/html/'
}

var sassOptions = {
	outputStyle: 		'compressed',
	errLogToConsole: true,
};

var prefixerOptions = {
	browsers: 			['last 2 versions', '> 5%', 'Firefox ESR']
};

// BrowserSync
gulp.task('bs', function() {
	browserSync({
		// Play Port
		proxy: 'localhost:9000',
		// BrowserSync Port
		port: 9001,
		// Open browser on activator run
		open: false
	});
});

// BrowserSync Reload Task
gulp.task('bs-reload', function() {
	browserSync.reload();
});

// Sass Compilation + Autoprefixer + Injection
gulp.task('sass', function() {
		return gulp
			.src(src.scss)
				.pipe(sass(sassOptions).on('error', sass.logError))
				.pipe(prefix(prefixerOptions))
				.pipe(gulp.dest(dest.css))
				.pipe(reload({stream:true}));
});

gulp.task('watch', ['bs'], function() {
	gulp.watch([src.scss], ['sass']);
	gulp.watch([src.scala, src.js], ['bs-reload']);
});

gulp.task('clean', function() {});

gulp.task('default', ['watch']);
gulp.task('build', ['sass']);

// If other sbt tasks are missing, check https://github.com/mmizutani/sbt-play-gulp#how-this-works


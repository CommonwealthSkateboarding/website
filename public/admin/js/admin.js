$(function() {
    $('#nav-accordion').dcAccordion({
        eventType: 'click',
        autoClose: true,
        saveState: true,
        disableLink: true,
        speed: 'slow',
        showCount: false,
        autoExpand: true,
//        cookie: 'dcjq-accordion-1',
        classExpand: 'dcjq-current-parent'
    });
    $('#sidebar .sub-menu > a').click(function () {
            var o = ($(this).offset());
            diff = 250 - o.top;
            if(diff>0)
                $("#sidebar").scrollTo("-="+Math.abs(diff),500);
            else
                $("#sidebar").scrollTo("+="+Math.abs(diff),500);
        });

    //    sidebar toggle

        $(function() {
            function responsiveView() {
                var wSize = $(window).width();
                if (wSize <= 768) {
                    $('#container').addClass('sidebar-close');
                    $('#sidebar > ul').hide();
                }

                if (wSize > 768) {
                    $('#container').removeClass('sidebar-close');
                    $('#sidebar > ul').show();
                }
            }
            $(window).on('load', responsiveView);
            $(window).on('resize', responsiveView);
        });

        $('.fa-bars').click(function () {
            if ($('#sidebar > ul').is(":visible") === true) {
                $('#main-content').css({
                    'margin-left': '0px'
                });
                $('#sidebar').css({
                    'margin-left': '-210px'
                });
                $('#sidebar > ul').hide();
                $("#container").addClass("sidebar-closed");
            } else {
                $('#main-content').css({
                    'margin-left': '210px'
                });
                $('#sidebar > ul').show();
                $('#sidebar').css({
                    'margin-left': '0'
                });
                $("#container").removeClass("sidebar-closed");
            }
        });

    // widget tools

        $('.panel .tools .fa-chevron-down').click(function () {
            var el = jQuery(this).parents(".panel").children(".panel-body");
            if (jQuery(this).hasClass("fa-chevron-down")) {
                jQuery(this).removeClass("fa-chevron-down").addClass("fa-chevron-up");
                el.slideUp(200);
            } else {
                jQuery(this).removeClass("fa-chevron-up").addClass("fa-chevron-down");
                el.slideDown(200);
            }
        });

        $('.panel .tools .fa-times').click(function () {
            jQuery(this).parents(".panel").parent().remove();
        });


    //    tool tips

        $('.tooltips').tooltip();

    //    popovers

        $('.popovers').popover();
});
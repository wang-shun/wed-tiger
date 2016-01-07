require.config({
	//baseUrl: '',	// 当前文件为根路径
	// 非AMD模块输出，将非标准的AMD模块"垫"成可用的模块
	shim: {
		'jquery.validate': ['jquery'],
		'semantic-ui-modal': ['semantic'],
		'semantic-ui-form': ['semantic'],
		'semantic-ui-transition': {deps: ['semantic'], exports: 'semantic-ui-transition' }	// 设置依赖文件和别名
	},
    paths: {
        'jquery'					:	'../plugin/jquery/jquery.2.1.4.min',
        'jquery.validate'			: 	'../plugin/jquery/jquery.validate.1.13.1.min',
        'semantic'					:	'../plugin/semantic-ui/dist/semantic.min',
        'semantic-ui-modal' 		: 	'../plugin/semantic-ui/dist/components/modal',
        'semantic-ui-form' 			: 	'../plugin/semantic-ui/dist/components/form',
        'semantic-ui-transition' 	:	'../plugin/semantic-ui/dist/components/transition',
        'semantic-comalert'			:	'../js/semantic-comalert',
        'ace'						:	'../plugin/ace/',						// path支持设置整个目录
        'source-code-pro'			:	'../plugin/jquery/jquery.2.1.4.min'	// path支持设置单个文件
    },
    waitSeconds : 5,	// TimeOut, 默认7秒
	urlArgs: "bust=" + (new Date()).getTime()	// 版本号, 防止缓存
});
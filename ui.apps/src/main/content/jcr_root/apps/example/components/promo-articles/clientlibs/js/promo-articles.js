/* eslint-disable */
RocheEcosys.Components.PromoArticles = function(el) {
    'use strict';
    var $component = el;
    var urlParam;
    var vueJsApp = {};

    var emptyPlaceholder = el.find('.cmp-teaser.hidden');
    if (emptyPlaceholder) {
        emptyPlaceholder.remove();
    }

    /**
     * Initialize the component
     */
    var initialize = function() {

        Vue.config.devtools = true;

        Vue.component('promo-articles', {
            props: {
                loadSize: {
                    type: Number
                },
                bufferMultiplier: {
                    type: Number
                },
                initialOffset: {
                    type: Number
                },
				pageSize : {
                    type: Number
                },
                action: {
                    type: String
                },
                title: {
                    type: String
                },
                layout: {
                    type: String
                },
                viewMore: {
                    type: String
                },
                viewMoreLink: {
                    type: String
                },
                editModeClass: {
                    type: String
                }
            },
            data: function() {
                return {
                    moreBtnDisable: false,
                    articles: [],
                    buffer:[],
                    numberItemsTotal: 0,
                    initialLoad: true,
                    offset: this.initialOffset,
                    resourcePath: '',
                    page: 1,
                    storeInitialized: false,
                    bookmark: Granite.I18n.get("lblCEAdd"),
                    videoTag: Granite.I18n.get("lblCEVideo"),
                    publicationsTag: Granite.I18n.get("lblCERochePublications"),
                    loadMoreLabel: Granite.I18n.get("lblCELoadMore"),
                    backTop: Granite.I18n.get("lblCEBackToTop"),
                    noResults: Granite.I18n.get("phCENoResults"),
                    noResultsMessage: Granite.I18n.get("phCENoResultsMessage"),
                    titleMaxSize: 104
                };
            },
            mounted: function() {
                var _this = this;
                _this.resourcePath = _this.action;
                //ADD HISTORY
                $(window).on("popstate", function () {
                    var page = 1;
                    if (history.state && history.state.page){
                        page = history.state.page;
                    }
                    _this.reloadContent(page);
                });
                //CHECK PARAMS
                urlParam = _this.getUrlVars();
                if(_this.initialLoad && urlParam["page"]){
                    _this.page = urlParam["page"];
                }

                //INITIAL LOAD
                _this.loadMore();
            },

            computed: {
                progressCompleted: function() {
                    if(this.articles.length  >= this.numberItemsTotal){
                        return "full";
                    }
                    return " ";
                },
                getProgressWidth: function() {
                    return "width: " + (this.articles.length / this.numberItemsTotal * 100) + "%";
                },
                calculatedOffset: {
                    get: function() {
                        return this.offset;
                    },
                    set: function(value) {
                        this.offset = value;
                    }
                }
            },
            methods: {
                updateBookmarks : function(){
                    if(RocheEcosys.Profile.userID !== RocheEcosys.Constants.UserInfo.ANONYMOUS){
                        this.storeInitialized = true;
                    }
                },
                reloadContent: function (page){
                    this.moreBtnDisable= false;
                    this.articles= [];
                    this.buffer=[];
                    this.numberItemsTotal= 0;
                    this.initialLoad= true;
                    this.offset= this.initialOffset;
                    this.page =1;
                    if (page){
                        this.page = page;
                    }
                    this.loadMore();

                },
                getUrlVars: function() {
                    var vars = {};
                    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
                        vars[key] = value;
                    });
                    return vars;
                },
                loadMore: function(){
                    var _this = this;
                    if(!_this.initialLoad){
                        _this.page = parseInt(_this.page) + 1;
                        _this.updateHistory();
                    }

                    _this.checkBuffer().then(function(){
                        _this.getArticles();
                    });

                },
                fireEvent : function(event){
                    event.preventDefault();
                    if (this.storeInitialized){
                        var selector = $(event.currentTarget);
                        selector.addClass('disabled');
                        var bookmarkId = selector.data('bookmark-id');
                        var bookmarkResource = selector.data('bookmark-resource');
                        var bookmarkPath = selector.data('bookmark-path');
                        var details = {"bookmarkId" : bookmarkId,
                            "bookmarkResource": bookmarkResource,
                            "selector": selector,
                            "bookmarkPath": bookmarkPath};
                        var eventName =  "bookmark";
                        if(selector.hasClass("bookmarked")){
                            eventName =  "unbookmark";
                        }
                        var event;
                        if (typeof(window.CustomEvent) === 'function') {
                            event = new CustomEvent(eventName, {detail: details});
                        } else {
                            event = document.createEvent('CustomEvent',{detail: details});
                            event.initCustomEvent(eventName, true, false, null);
                        }
                        document.body.dispatchEvent(event);
                    } else {
                        RocheEcosys.Utils.LoginUtils.redirectToLoginPage();
                    }
                },
                updateHistory: function(){
                    var state = {page:this.page};
                    history.pushState(state, null, "?page="+this.page);
                },
                checkBuffer: function(){
                    var _this = this;
                    return new Promise(function (resolve) {
                        var action = _this.action.concat(".loadmore.");

                        if ((_this.pageSize > _this.buffer.length &&
                             ((_this.articles.length + _this.buffer.length) < _this.numberItemsTotal))||
                            _this.numberItemsTotal === 0){

                            if(_this.initialLoad){
                                _this.calculatedOffset = _this.pageSize*parseInt(_this.page) + _this.initialOffset ;
                            }else{
                                _this.calculatedOffset = _this.articles.length + _this.buffer.length;
                            }

                            /*if (!_this.initialLoad && _this.calculatedOffset > _this.numberItemsTotal){
                                _this.calculatedOffset = _this.numberItemsTotal;
                            }*/

                            if(_this.initialLoad){
                                var items = (_this.pageSize*2) +  _this.calculatedOffset;
                                action = action.concat(items).concat(".").concat(_this.initialOffset).concat('.').concat('articles').concat(".json");
                            } else {
                                action = action.concat(_this.pageSize*_this.bufferMultiplier).concat(".").concat(_this.calculatedOffset).concat('.').concat('articles').concat(".json");
                            }
                            axios.get(action).then(function(response) {
                                var data = response.data;
                                if (data) {
                                    if (_this.initialLoad) {
                                        _this.initialLoad = false;
                                    }
                                    _this.numberItemsTotal = data.totalResults;
                                    _this.buffer = _this.buffer.concat(data.results);
                                    resolve();
                                } else {
                                    _this.moreBtnDisable = true;
                                    resolve();
                                }
                            });
                        } else {
                            resolve();
                        }
                    });
                },
                getArticles: function(){
                    var _this = this;
                    return new Promise(function (resolve) {
                        if(_this.buffer.length > 0){

                            var limit = _this.pageSize;

                            if(_this.articles.length < 1){
                                limit = _this.pageSize*parseInt(_this.page);
                            }
                            if (limit > _this.buffer.length){
                                limit = _this.buffer.length;
                            }

                            for (var index = 0; index < limit ; index++) {
                                _this.articles.push(_this.buffer.shift());
                            }
                            if (_this.articles.length >= _this.numberItemsTotal) {
                                _this.moreBtnDisable = true;
                            }
                            resolve();
                        }
                    });
                },
                truncate: function(text,maxSize){
                    if (text !== null && text !== undefined){
                        var size = text.length;

                        if(size > maxSize) {
                            var truncated = text.slice(0, maxSize) + '...';
                            return truncated;
                        } else {
                            return text;
                        }
                    }                    
                }

            },
            updated: function () {
                this.$nextTick(function () {
                    if (ContextHub && ContextHub.getStore("bookmarks")){
                        var bookmarkStore = ContextHub.getStore("bookmarks").getTree(false);
                        var keys = Object.keys(bookmarkStore);
                        // transition all bookmark buttons to bookmarked
                        keys.forEach(function (key) {
                            var bookmarkId = bookmarkStore[key].id;
                            var $bookmark = $component.find("[data-bookmark-id=" + bookmarkId +"]");
                            $bookmark.each(function(){
                                if(!$(this).hasClass('bookmarked')){
                                    $(this).toggleClass("bookmarked");
                                    $(this).find(".hidden-text").html(Granite.I18n.get("lblCERemove"));
                                    $(this).find(".material-icons").html("bookmark");
                                }
                            });
                        });
                    }                    
                });
            },
            template: '#promo-articles-vue'
        });

        var promoArticles = el.find('#promo-articles');

        if (promoArticles.length > 0) {
            vueJsApp = new Vue({
                el: el.find('#promo-articles')[0]
            });
        }
        //ADD CONTEXT STORE CALLBACK
        if (ContextHub && ContextHub.getStore("bookmarks")){
            var bookmarkStore = ContextHub.getStore("bookmarks");
            if (bookmarkStore) {
                bookmarkStore.eventing.once(ContextHub.Constants.EVENT_ALL_STORES_READY, vueJsApp.$children[0].updateBookmarks, "updateBookmarks", true);
            }
        }
        
        return vueJsApp;
    };

    return {
        init: initialize
    };
};

$(document).ready(function() {
    'use strict';
    $('div.promo-articles-cmp').each(function(index, obj) {
       new RocheEcosys.Components.PromoArticles($(obj)).init();
    });
});

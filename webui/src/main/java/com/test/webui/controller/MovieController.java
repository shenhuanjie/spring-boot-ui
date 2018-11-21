package com.test.webui.controller;

import com.test.data.domain.Actor;
import com.test.data.domain.Movie;
import com.test.data.repositories.ActorRepository;
import com.test.data.repositories.MovieRepository;
import com.test.data.service.PagesService;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/movie")
public class MovieController {
    private static Logger logger = LoggerFactory.getLogger(MovieController.class);
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ActorRepository actorRepository;
    @Autowired
    private PagesService<Movie> pagesService;

    /**
     * INDEX
     *
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView index() {
        return new ModelAndView("movie/index");
    }

    /**
     * 查看电影控制器
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}")
    public ModelAndView show(ModelMap model, @PathVariable Long id) {
        Movie movie = movieRepository.findOne(id);
        model.addAttribute("movie", movie);
        return new ModelAndView("movie/show");
    }

    /**
     * 新建电影控制器
     *
     * @param model
     * @return
     */
    @RequestMapping("/new")
    public ModelAndView create(ModelMap model) {
        String[] files = {"/images/movie/西游记.jpg", "/images/movie/西游记续集.jpg"};
        model.addAttribute("files", files);
        return new ModelAndView("movie/new");
    }

    /**
     * 保存电影控制器
     *
     * @param movie
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(Movie movie) {
        movieRepository.save(movie);
        logger.info("新增->ID={}", movie.getId());
        return "1";
    }

    /**
     * 修改电影控制器
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/edit/{id}")
    public ModelAndView update(ModelMap model, @PathVariable Long id) {
        Movie movie = movieRepository.findOne(id);
        String[] files = {"/images/movie/西游记.jpg", "/images/movie/西游记续集.jpg"};
        String[] rolelist = new String[]{"唐僧", "孙悟空", "猪八戒", "沙僧"};
        Iterable<Actor> actors = actorRepository.findAll();

        model.addAttribute("files", files);
        model.addAttribute("rolelist", rolelist);
        model.addAttribute("movie", movie);
        model.addAttribute("actors", actors);

        return new ModelAndView("movie/edit");
    }

    /**
     * 更新电影控制器
     *
     * @param movie
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/update")
    public String update(Movie movie, HttpServletRequest request) {
        String rolename = request.getParameter("rolename");
        String actorid = request.getParameter("actorid");

        Movie old = movieRepository.findOne(movie.getId());
        old.setName(movie.getName());
        old.setPhoto(movie.getPhoto());
        old.setCreateDate(movie.getCreateDate());

        if (!StringUtils.isEmpty(rolename) && !StringUtils.isEmpty(actorid)) {
            Actor actor = actorRepository.findOne(new Long(actorid));
            old.addRole(actor, rolename);
        }
        movieRepository.save(old);
        logger.info("修改->ID=" + old.getId());
        return "1";
    }

    /**
     * 删除电影控制器
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable Long id) {
        Movie movie = movieRepository.findOne(id);
        movieRepository.delete(movie);
        logger.info("删除->ID=" + id);
        return "1";
    }

    /**
     * 电影分页查询控制器
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/list")
    public Page<Movie> list(HttpServletRequest request) {
        String name = request.getParameter("name");
        String page = request.getParameter("page");
        String size = request.getParameter("size");
        Pageable pageable = new PageRequest(page == null ? 0 : Integer.parseInt(page), size == null ? 10 : Integer.parseInt(size),
                new Sort(Sort.Direction.DESC, "id"));

        Filters filters = new Filters();
        if (!StringUtils.isEmpty(name)) {
            Filter filter = new Filter("name", name);
            filters.add(filter);
        }

        return pagesService.findAll(Movie.class, pageable, filters);
    }
}

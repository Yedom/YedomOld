package ru.mralexeimk.yedom.controllers;

import org.jgrapht.Graphs;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.mralexeimk.yedom.models.TagsGraphNode;
import ru.mralexeimk.yedom.models.User;
import ru.mralexeimk.yedom.services.FriendsService;
import ru.mralexeimk.yedom.services.RolesService;
import ru.mralexeimk.yedom.services.TagsService;
import ru.mralexeimk.yedom.services.UtilsService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/graphs")
public class GraphsController {
    private final UtilsService utilsService;
    private final RolesService rolesService;
    private final TagsService tagsService;
    private final FriendsService friendsService;

    public GraphsController(UtilsService utilsService, RolesService rolesService, TagsService tagsService, FriendsService friendsService) {
        this.utilsService = utilsService;
        this.rolesService = rolesService;
        this.tagsService = tagsService;
        this.friendsService = friendsService;
    }

    @GetMapping("/tags")
    public String getTagsGraph(Model model) {
        model.addAttribute("endpoint", "/graphs/tagsData");
        return "graphs/graph";
    }

    @GetMapping("/friends")
    public String getFriendsGraph(Model model) {
        model.addAttribute("endpoint", "/graphs/friendsData");
        return "graphs/graph";
    }

    @GetMapping(value = "/tagsData", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getTagsGraphData(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        if(!rolesService.hasPermission(user, "graphs.display"))
            return "";

        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        Set<String> tags = tagsService.getGraph().vertexSet();

        // add nodes and edges
        for(String tag : tags) {
            TagsGraphNode nodeInfo = tagsService.getGraphInfo().get(tag);
            JSONObject node = new JSONObject();
            node.put("id", nodeInfo.getIndex());
            node.put("label", tag);
            node.put("shape", "dot");
            node.put("color", "#97c2fc");
            nodes.put(node);

            Graphs.neighborListOf(tagsService.getGraph(), tag).forEach(neighbor -> {
                JSONObject edge = new JSONObject();
                edge.put("arrows", "to");
                edge.put("from", nodeInfo.getIndex());
                edge.put("to", tagsService.getGraphInfo().get(neighbor).getIndex());
                edges.put(edge);
            });
        }

        return utilsService.jsonToString(
                List.of(nodes.toString(), edges.toString()),
                List.of("nodes", "edges"));
    }

    @GetMapping(value = "/friendsData", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getFriendsGraphData(HttpSession session) {
        String check = utilsService.preventUnauthorizedAccess(session);
        if(check != null) return check;

        User user = (User) session.getAttribute("user");

        if(!rolesService.hasPermission(user, "graphs.display"))
            return "";

        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();

        Set<Integer> usersIds = friendsService.getFriendsGraph().vertexSet();

        // add nodes and edges
        for(Integer id : usersIds) {
            JSONObject node = new JSONObject();
            node.put("id", id);
            node.put("label", id);
            node.put("shape", "dot");
            node.put("size", 10);
            node.put("color", "#97c2fc");
            nodes.put(node);

            Graphs.neighborListOf(friendsService.getFriendsGraph(), id).forEach(neighbor -> {
                JSONObject edge = new JSONObject();
                edge.put("width", 1);
                edge.put("from", id);
                edge.put("to", neighbor);
                edges.put(edge);
            });
        }

        return utilsService.jsonToString(
                List.of(nodes.toString(), edges.toString()),
                List.of("nodes", "edges"));
    }
}

package br.com.lGabrielDev.tests_bolados.models.person;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.lGabrielDev.tests_bolados.exceptions.NameCannotBeNullException;
import br.com.lGabrielDev.tests_bolados.exceptions.PersonNotFoundException;
import br.com.lGabrielDev.tests_bolados.models.Person.Person;
import br.com.lGabrielDev.tests_bolados.models.Person.PersonController;
import br.com.lGabrielDev.tests_bolados.models.Person.PersonService;
import br.com.lGabrielDev.tests_bolados.models.Person.dtos.PersonCreateDTO;


@WebMvcTest(PersonController.class) //informamos a controller class que vamos testar. É injetado automaticamente um objeto PersonController.
public class PersonControllerTest {
    

    //injected attributes
    @Autowired
    private MockMvc mockMvc; //vamos usar esse objeto para referenciar os endpoints/rotas da nossa controller.

    @Autowired
    private ObjectMapper transformadorJson; //usamos esse cara/objeto para converter objetos do Java em objetos JSON e vice-versa.

    @MockBean //vamos fingir/"mockar" o comportamento dos methods da class abaixo
    private PersonService ps;



    //tests

    

 // ================================= READ =================================

    // ============= FindAll =============
    @Test
    public void theResponseShouldReturn200OkStatusAndInsideTheBodyShouldReturnAListOfPersons() throws Exception{
        //arrange
        List<Person> personas = List.of(
            new Person("Sonic","sonic@gmail.com"),
            new Person("sasuke","uchiha@gmail.com")
        );

        String conteudoDoBody = this.transformadorJson.writeValueAsString(personas); //transformamos um objeto java em uma String JSON.

        Mockito.when(this.ps.getAllPersons()).thenReturn(personas); //definimos o comportamento do method mockado.

        //act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/person")) //informamos que vamos testar a rota "/person" do tipo GET. Ao memso tempo, executamos esse method.

        //assert
            .andExpect(MockMvcResultMatchers.status().isOk()) //afirmamos que a resposta será um responseEntity(respostaHTTP) OK.
            .andExpect(MockMvcResultMatchers.content().json(conteudoDoBody)) //afirmamos que o conteudo do body dessa resposta http será um json, em seguida informamos esse json.
            .andDo(MockMvcResultHandlers.print()); //printamos no terminal as informacoes desse test.
    }



    // ============= findByID =============
    @Test
    @DisplayName("User Should put the personID and get the response 200 OK with a person inside the body")
    public void itShouldReturnA200OkStatusWithAPersonInsideTheBody() throws Exception{

        //arrange
        Person p1 = new Person();
        p1.setId(44l);
        p1.setName("sonic");
        p1.setEmail("sonic@gmail.com");

        Mockito.when(this.ps.getPersonById(Mockito.anyLong())).thenReturn(p1); //simulamos o comportamento do method mockado da service.

        //act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/person/{id}", Mockito.anyLong())) //informamos a rota/method que queremos testar. Perceba que o get() recebe como parametro 1- a URL 2- pathvariable
            
        //assert
            .andExpect(MockMvcResultMatchers.status().isOk()) //Esperamos um ResponseEntity 200 OK
            .andExpect(MockMvcResultMatchers.content().json(this.transformadorJson.writeValueAsString(p1))) //esperamos que o conteudo do body dessa resposta HTTP seja um JSON. Note que Precisamos transformar um objeto JAva em um JSON. Por isso, precisamos do ObjectMapper.
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(p1.getName())) //esperamos que o valor do attribute "name" desse JSON seja "sonic"
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(p1.getEmail())) //esperamos que o valor do attribute "email" desse JSON seja "sonic@gmail.com"
            .andDo(MockMvcResultHandlers.print()); //printamos no terminal a solicitacao http enviada E a resposta dessa requisicao
    }


    @Test
    @DisplayName("It shouldn't return a person, because the #personID is wrong. It Should return a 400")
    public void itShouldReturnANotFoundStatusBecauseThePersonIdIsWrong() throws Exception{
        //arrange
        Long personId = 50l;

        Mockito.when(this.ps.getPersonById(personId)).thenThrow(new PersonNotFoundException("Person not found")); //simulamos que o methods da service nao encontrou uma person com o id informado. Portanto, ele lanca uma exception
        
        //act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/person/{id}", personId)) //informamos a rota/method que vamos testar. Do tipo GET
        //assert
            .andExpect(MockMvcResultMatchers.status().isNotFound()) // afirmamos que o status dessa reposta HTTP vai ser um status 404
            .andDo(MockMvcResultHandlers.print()); //printamos no terminal as informacoes do request e da resposta

    }



    // ============= FindByEmail =============
    @Test
    @DisplayName("It should return a 200 OK Status and the body must constains a person")
    public void itShouldReturn200OkAndTheBodyMustContainsAPerson() throws Exception{

        //arrange
        String emailProcurado = "sonic@gmail.com";
        Person pessoaDoBody = new Person("sonic", emailProcurado);
        Mockito.when(this.ps.getPersonByEmail(emailProcurado)).thenReturn(pessoaDoBody); //fingimos que esse methods mockado vai receber um "email" e retornar uma "Pessoa"

        //act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/person/by-email/{email}", emailProcurado))

        //assert
            .andExpect(MockMvcResultMatchers.status().isOk()) //afirmamos que a resposta HTTP vai ter um Status 200 OK
            .andExpect(MockMvcResultMatchers.content().json(this.transformadorJson.writeValueAsString(pessoaDoBody))) //afirmamos que o conteudo do body desse ResponseEntity vai ser um JSON, que no caso é aquela "Person" que criamos. Como o Json deve ser uma String, precisamos transformar a nosso objeto java em um JSON.
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(pessoaDoBody.getName())) //afirmamos que o valor do attribute "name" desse JSON vai ser "sonic"
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(pessoaDoBody.getEmail())) //afirmamos que o valor do attribute "email" desse JSON vai ser "sonic@gmail.com"
            .andDo(MockMvcResultHandlers.print()); //debugando as informacoes do request e da response

    }

    @Test
    @DisplayName("It should return a 404 not found status, because it doesn't exists a person with this 'email' ")
    public void itSohuldReturn404NotFoundBecauseThereIsNoPersonWithThisEmail() throws Exception{
        //arrange
        String email = "sonic@gmail.com";

        Mockito.when(this.ps.getPersonByEmail(email)).thenThrow(new PersonNotFoundException("Person not found")); //definimos que o comportamento desse method "mockado" vai lancar uma Exception

        //act
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/person/by-email/{email}", email)) //executamos o method/rota que vamos testar
        //assert
            .andExpect(MockMvcResultMatchers.status().isNotFound()) //afirmamos que o status recebido será um 404
            .andExpect(MockMvcResultMatchers.content().string("Person not found")) //afirmamos que o o body dessa responseEntity será essa.
            .andDo(MockMvcResultHandlers.print()); //usamos para debugar
    }


    // ================================= CREATE =================================
    @Test
    @DisplayName("It should create a person successfully")
    public void itShouldCreateAPersonSuccessFully() throws Exception{
        //arrange
        PersonCreateDTO personInformadaNoBody = new PersonCreateDTO("sonic", "sonic@gmail.com");
        Person p1 = new Person();
        p1.setId(1l);
        p1.setName(personInformadaNoBody.getName());
        p1.setEmail(personInformadaNoBody.getEmail());


        Mockito.when(this.ps.createPerson(Mockito.any())).thenReturn(p1); //simulamos o comportamento do method mockado da service.

        //act
        //perceba que nós executamos o method/rota igualzinho como se estivessemos fazendo o request no postman. Passando o request body, pathVariables, requestParams, etc...
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/person") //informamos a rota/methods que vamos testar
            .contentType(MediaType.APPLICATION_JSON) //o tipo do conteudo que vamos informar no request body é um JSON padraozin
            .content(this.transformadorJson.writeValueAsString(personInformadaNoBody)) //informamos o request body
        ) 
            
        //assert
            .andDo(MockMvcResultHandlers.print()); //printamos para debugar
            
    }


    @Test
    @DisplayName("It should get a 409 CONFLICT, because the 'name' was not informed")
    public void itShouldReturn409ConflictBecauseTheNameAttributeWasNotInformed() throws Exception{
        
        //arrange
        PersonCreateDTO personInformadaNoBody = new PersonCreateDTO();
        personInformadaNoBody.setName(null); //simulamos que esse campo nao foi informado
        personInformadaNoBody.setEmail("sonic@gmail.com");

        Mockito.when(this.ps.createPerson(Mockito.any())).thenThrow(new NameCannotBeNullException("'Name' cannot be null")); //vamos pegar esse method mockado e simular o comportamento dele. Nesse caso, nós estamos simulando que ele vá receber um DTO com o "name" incompleto e vai retornar uma exception.

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .post("/api/person") //rota/method que vamos testar.
            .contentType(MediaType.APPLICATION_JSON) // informamos o tipo do objeto que vamos enviar no @RequestBody
            .content(this.transformadorJson.writeValueAsString(personInformadaNoBody)) //conteudo que vamos informar no body da requisicao @RequestBody
        )

        //assert
        .andExpect(MockMvcResultMatchers.status().isConflict()) //afirfamos que essa rota vai retornar um 409 CONFLICT
        .andDo(MockMvcResultHandlers.print()); //printar para debugar
    }

   
    // ================================= UPDATE =================================
    @Test
    @DisplayName("It should update a person successfully")
    public void itShouldUpdateAPersonSuccessfully() throws Exception{
        //arrange
        Person personAntiga = new Person(); //representando os dados antigos de uma person do banco
        personAntiga.setId(20l);


        PersonCreateDTO dadosNovosDto = new PersonCreateDTO( //representando os dados novos
            "monica",
            "monica@gmail.com"
        );

        Person novaPessoa = new Person(); //representando a pessoa com os dados atualizados
        novaPessoa.setId(personAntiga.getId());
        novaPessoa.setName(dadosNovosDto.getName());
        novaPessoa.setEmail(dadosNovosDto.getEmail());

    
        Mockito.when(this.ps.updatePerson(Mockito.anyLong(), Mockito.any())).thenReturn(novaPessoa);  //simulamos que esse method "mockado" vai receber 2 parametros (personID and DTO) e vai retornar uma "person" com os dados atualizados //sempre que usar o mockito.any(), todos os parametros devem ser any()

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .put("/api/person/{id}", personAntiga.getId())
            .contentType(MediaType.APPLICATION_JSON) //tipo do objeto informado no @RequestBody
            .content(this.transformadorJson.writeValueAsString(dadosNovosDto)) //informamos o DTO no @RequestBody
        )

        //assert
        .andExpect(MockMvcResultMatchers.status().isOk()) //afirmamos que a resposta dessa requeste é 200 OK
        .andExpect(MockMvcResultMatchers.content().json(this.transformadorJson.writeValueAsString(novaPessoa))) //afirmamos que o objeto JSON que vai vir no body é o JSON "tal"
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(novaPessoa.getName())) //afirmamos que o attribute "name" do JSON retornado é "monica"
        .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(novaPessoa.getEmail())) //afirmamos que o attribute "email" do JSON retornado é "monica@gmail.com"
        .andDo(MockMvcResultHandlers.print()); //printando para debugar
    }



    @Test
    @DisplayName("It should get a 404 NOT FOUND because the personId doesn't exists")
    public void itShouldReturn404NotFoundBecauseThePersonIdDoesntExists() throws Exception{
        //arrange
        Long personId = 20l;

        PersonCreateDTO dtozinho = new PersonCreateDTO("sasuke", "uchiha@gmail.com");

        Mockito.when(this.ps.updatePerson(Mockito.anyLong(), Mockito.any())).thenThrow(new PersonNotFoundException("Nao eh possivel alterar, pois nao existe uma pessoa com esse #id")); //Vamos definir o que esse method "mockado" vai fazer. Nesse caso, ele recebe 2 parametros, ambos mockito any(), e retorna uma exception. Lembrando que sempre que voce usar o Mockito.any(), use em todos os parametros.

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .put("/api/person/{id}", personId) //essa eh a rota que vamos testar
            .contentType(MediaType.APPLICATION_JSON) //o tipo do objeto que vamos passar no @RequestBody eh um JSON
            .content(this.transformadorJson.writeValueAsString(dtozinho)) //esse é o JSON que vmaos informar no @RequestBody
        )

        //assert
        .andExpect(MockMvcResultMatchers.status().isNotFound()) //afirmamos que o status HTTP será um 404 NOT FOUND
        .andExpect(MockMvcResultMatchers.content().string("Nao eh possivel alterar, pois nao existe uma pessoa com esse #id")) //afirmamos que o conteudo da resposta HTTP será essa String.
        .andDo(MockMvcResultHandlers.print()); //printamos para debugar
    }


    @Test
    @DisplayName("It should get a 409 CONFLICT because the 'name' is null")
    public void itShouldGet400BadRequestBecauseNameIsNull() throws Exception{
        //arrange
        Long personId = 13l;

        PersonCreateDTO dtozinho = new PersonCreateDTO();
        dtozinho.setName(null); //simulamos que o cliente nao vai preencher o campo "name"
        dtozinho.setEmail("maria@gmail.com");

        Mockito.when(this.ps.updatePerson(Mockito.anyLong(), Mockito.any())).thenThrow(new NameCannotBeNullException("Name cannot be null")); 

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .put("/api/person/{id}", personId) //vamos testar essa rota/method
            .contentType(MediaType.APPLICATION_JSON) //definimos qual eh o tipo do objeto enviado no body do request
            .content(this.transformadorJson.writeValueAsString(dtozinho)) //informamos qual e o objeto JSON que vai ser enviado no @RequestBody
        )

        //assert
        .andExpect(MockMvcResultMatchers.status().isConflict()) //afirmamos que os tatus sera um 409 CONFLICT
        .andExpect(MockMvcResultMatchers.content().string("Name cannot be null"))
        .andDo(MockMvcResultHandlers.print()); //printamos pra debugar
    }
   
    // ================================= DELETE =================================
    @Test
    @DisplayName("It should return a successfully delete message")
    public void ItSohuldReturnASuccessfullyDeleteMessage() throws Exception{
        //arrange
        Long personId = 35l;
        String successMessage = "deletado com suceso!";

        Mockito.when(this.ps.deletePerson(personId)).thenReturn(successMessage); //simulamos o comportamento desse method "mockado".

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/person/{id}", personId)
        )
        //assert
        .andExpect(MockMvcResultMatchers.status().isOk()) //afirmamos que o status será 200 OK
        .andExpect(MockMvcResultMatchers.content().string("deletado com suceso!"))
        .andDo(MockMvcResultHandlers.print()); //printar para debugar
    }


    @Test
    @DisplayName("It should get a 409 CONFLICT because the personId doesn't exists")
    public void itShouldThrowAnExceptionBecausePersonIdDoesntExists() throws Exception {
        //arrange
        Long personId = 10l;

        Mockito.when(this.ps.deletePerson(personId)).thenThrow(new NameCannotBeNullException("Cannnot delete this person because the personId doesn't exists"));

        //act
        this.mockMvc.perform(MockMvcRequestBuilders
            .delete("/api/person/{id}", personId)
        )

        //assert
        .andDo(MockMvcResultHandlers.print()); //printamos para debugar.
    }
}

